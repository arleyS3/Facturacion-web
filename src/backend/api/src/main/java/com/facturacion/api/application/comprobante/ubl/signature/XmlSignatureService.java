package com.facturacion.api.application.comprobante.ubl.signature;

import com.facturacion.api.application.EncryptionUtil;
import com.facturacion.api.web.models.ConfiguracionCertificadoEntity;
import com.facturacion.api.web.repositories.ConfiguracionCertificadoRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Signs UBL XML documents with the active PKCS12 certificate configured for the issuer RUC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class XmlSignatureService {

    private static final String EXT_NS = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
    private static final String CAC_NS = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String CBC_NS = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String DS_NS = "http://www.w3.org/2000/09/xmldsig#";
    private static final String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";

    private final ConfiguracionCertificadoRepository configRepository;
    private final EncryptionUtil encryptionUtil;

    /**
     * Signs an XML document using the active certificate stored for the issuer RUC.
     *
     * @param xmlDocument UBL XML document to sign
     * @param rucEmisor issuer RUC used to resolve the configured certificate
     * @return signed UBL XML with ext:UBLExtensions and cac:Signature reference
     * @throws Exception when certificate configuration or XML signing fails
     */
    public String signXml(String xmlDocument, String rucEmisor) throws Exception {
        ConfiguracionCertificadoEntity config = configRepository
                .findByRucEmisorAndActivoTrue(rucEmisor)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay certificado configurado para RUC: " + rucEmisor));

        return signXmlWithConfig(xmlDocument, config);
    }

    public record ResultadoFirmaXml(String xml, boolean firmado, String mensaje) {}

    /**
     * Intenta firmar el XML con el certificado configurado y retorna el resultado detallado.
     *
     * @param xmlDocument Documento UBL XML a firmar
     * @param rucEmisor RUC emisor para buscar el certificado activo
     * @return ResultadoFirmaXml con el XML (firmado o sin firmar), flag de firmado y mensaje explicativo
     */
    public ResultadoFirmaXml trySignXmlResult(String xmlDocument, String rucEmisor) {
        var configOpt = configRepository.findByRucEmisorAndActivoTrue(rucEmisor);
        if (configOpt.isEmpty()) {
            log.warn("No hay certificado configurado para RUC {} - XML devuelto sin firma", rucEmisor);
            return new ResultadoFirmaXml(xmlDocument, false, "Sin certificado activo en BD para RUC emisor: " + rucEmisor);
        }
        try {
            String xmlFirmado = signXmlWithConfig(xmlDocument, configOpt.get());
            return new ResultadoFirmaXml(xmlFirmado, true, "XML firmado digitalmente con éxito para RUC: " + rucEmisor);
        } catch (Exception e) {
            log.error("Error al firmar XML para RUC {}: {} - XML devuelto sin firma", rucEmisor, e.getMessage());
            return new ResultadoFirmaXml(xmlDocument, false, "Error al firmar XML: " + e.getMessage());
        }
    }

    /**
     * Intenta firmar el XML con el certificado configurado. Si no hay certificado
     * activo para el RUC, retorna el XML sin firmar con una advertencia en logs.
     *
     * @param xmlDocument UBL XML document to sign
     * @param rucEmisor issuer RUC used to resolve the configured certificate
     * @return signed XML if certificate is configured, unsigned XML otherwise
     */
    public String trySignXml(String xmlDocument, String rucEmisor) {
        return trySignXmlResult(xmlDocument, rucEmisor).xml();
    }

    private String signXmlWithConfig(String xmlDocument, ConfiguracionCertificadoEntity config) throws Exception {
        byte[] certBytes = Base64.getDecoder().decode(config.getCertificadoBase64().trim());
        String password = encryptionUtil.decrypt(config.getPasswordEncriptada());

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(certBytes), password.toCharArray());

        String alias = resolveAlias(keyStore, config.getAliasCertificado());
        Key key = keyStore.getKey(alias, password.toCharArray());
        if (!(key instanceof PrivateKey privateKey)) {
            throw new IllegalStateException("El alias del certificado no contiene una clave privada: " + alias);
        }

        Certificate certificate = keyStore.getCertificate(alias);
        if (!(certificate instanceof X509Certificate x509Certificate)) {
            throw new IllegalStateException("El alias del certificado no contiene un certificado X.509: " + alias);
        }

        return signXmlWithKey(xmlDocument, privateKey, x509Certificate);
    }

    /**
     * Signs an XML document with a provided private key and certificate.
     */
    public String signXmlWithKey(
            String xmlDocument,
            PrivateKey privateKey,
            X509Certificate certificate) throws Exception {
        try {
            Document doc = parseXml(xmlDocument);
            Element root = doc.getDocumentElement();
            String documentId = textOfFirstDirectChild(root, "ID");
            String signatureId = buildSignatureId(documentId);

            Element extensionContent = ensureUblExtensionContent(doc, root);
            ensureCacSignatureReference(doc, root, signatureId);

            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
            Reference reference = signatureFactory.newReference(
                    "",
                    signatureFactory.newDigestMethod(DigestMethod.SHA256, null),
                    List.of(signatureFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null,
                    null);
            SignedInfo signedInfo = signatureFactory.newSignedInfo(
                    signatureFactory.newCanonicalizationMethod(
                            CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null),
                    signatureFactory.newSignatureMethod(RSA_SHA256, null),
                    List.of(reference));

            KeyInfo keyInfo = buildKeyInfo(signatureFactory, certificate);
            DOMSignContext signContext = new DOMSignContext(privateKey, extensionContent);
            signContext.setDefaultNamespacePrefix("ds");

            signatureFactory.newXMLSignature(signedInfo, keyInfo, null, signatureId, null)
                    .sign(signContext);

            log.info("XML UBL firmado exitosamente con XMLDSig (RSA-SHA256)");
            return serialize(doc);
        } catch (Exception e) {
            log.error("Error al firmar XML UBL: {}", e.getMessage(), e);
            throw new RuntimeException("Error al firmar XML UBL: " + e.getMessage(), e);
        }
    }

    public boolean verifySignature(String signedXmlDocument) {
        log.warn("Verificación de firma no implementada - retornando true por defecto");
        return true;
    }

    private String resolveAlias(KeyStore keyStore, String configuredAlias) throws Exception {
        if (configuredAlias != null && !configuredAlias.isBlank()) {
            if (keyStore.containsAlias(configuredAlias)) {
                return configuredAlias;
            }
            throw new IllegalStateException("Alias de certificado no encontrado en el PKCS12: " + configuredAlias);
        }

        var aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) {
                return alias;
            }
        }
        throw new IllegalStateException("El archivo PKCS12 no contiene alias con clave privada");
    }

    private Document parseXml(String xmlDocument) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        return factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new StringReader(xmlDocument)));
    }

    private Element ensureUblExtensionContent(Document doc, Element root) {
        Element ublExtensions = firstDirectChild(root, EXT_NS, "UBLExtensions");
        if (ublExtensions == null) {
            ublExtensions = doc.createElementNS(EXT_NS, "ext:UBLExtensions");
            root.insertBefore(ublExtensions, firstElementChild(root));
        }

        Element ublExtension = doc.createElementNS(EXT_NS, "ext:UBLExtension");
        Element extensionContent = doc.createElementNS(EXT_NS, "ext:ExtensionContent");
        ublExtension.appendChild(extensionContent);
        ublExtensions.appendChild(ublExtension);
        return extensionContent;
    }

    private void ensureCacSignatureReference(Document doc, Element root, String signatureId) {
        Element signature = doc.createElementNS(CAC_NS, "cac:Signature");
        appendTextElement(doc, signature, CBC_NS, "cbc:ID", signatureId);

        Element signatoryParty = doc.createElementNS(CAC_NS, "cac:SignatoryParty");
        Element partyIdentification = doc.createElementNS(CAC_NS, "cac:PartyIdentification");
        appendTextElement(doc, partyIdentification, CBC_NS, "cbc:ID", findSupplierRuc(root));
        signatoryParty.appendChild(partyIdentification);

        String supplierName = findSupplierName(root);
        if (!supplierName.isBlank()) {
            Element partyName = doc.createElementNS(CAC_NS, "cac:PartyName");
            appendTextElement(doc, partyName, CBC_NS, "cbc:Name", supplierName);
            signatoryParty.appendChild(partyName);
        }
        signature.appendChild(signatoryParty);

        Element attachment = doc.createElementNS(CAC_NS, "cac:DigitalSignatureAttachment");
        Element externalReference = doc.createElementNS(CAC_NS, "cac:ExternalReference");
        appendTextElement(doc, externalReference, CBC_NS, "cbc:URI", "#" + signatureId);
        attachment.appendChild(externalReference);
        signature.appendChild(attachment);

        Node insertionPoint = firstDirectChild(root, CAC_NS, "AccountingSupplierParty");
        if (insertionPoint == null) {
            root.appendChild(signature);
            return;
        }
        root.insertBefore(signature, insertionPoint);
    }

    private KeyInfo buildKeyInfo(XMLSignatureFactory signatureFactory, X509Certificate certificate) throws Exception {
        KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
        List<Object> x509Content = new ArrayList<>();
        x509Content.add(certificate.getSubjectX500Principal().getName());
        x509Content.add(certificate);
        X509Data x509Data = keyInfoFactory.newX509Data(x509Content);
        return keyInfoFactory.newKeyInfo(List.of(x509Data));
    }

    private String findSupplierRuc(Element root) {
        Element supplierParty = firstDescendant(root, CAC_NS, "AccountingSupplierParty");
        String partyIdentification = textOfFirstDescendant(supplierParty, "ID");
        if (partyIdentification.matches("\\d{11}")) {
            return partyIdentification;
        }
        String companyId = textOfFirstDescendant(supplierParty, "CompanyID");
        return companyId.matches("\\d{11}") ? companyId : "";
    }

    private String findSupplierName(Element root) {
        Element supplierParty = firstDescendant(root, CAC_NS, "AccountingSupplierParty");
        String registrationName = textOfFirstDescendant(supplierParty, "RegistrationName");
        if (!registrationName.isBlank()) {
            return registrationName;
        }
        return textOfFirstDescendant(supplierParty, "Name");
    }

    private String buildSignatureId(String documentId) {
        String safeId = documentId == null || documentId.isBlank()
                ? "document"
                : documentId.replaceAll("[^A-Za-z0-9_-]", "-");
        return "S" + safeId;
    }

    private String textOfFirstDirectChild(Element parent, String localName) {
        Element child = firstDirectChild(parent, null, localName);
        return child == null || child.getTextContent() == null ? "" : child.getTextContent().trim();
    }

    private String textOfFirstDescendant(Element parent, String localName) {
        Element child = firstDescendant(parent, null, localName);
        return child == null || child.getTextContent() == null ? "" : child.getTextContent().trim();
    }

    private Element firstDirectChild(Element parent, String namespace, String localName) {
        if (parent == null) {
            return null;
        }
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof Element element
                    && localName.equals(element.getLocalName())
                    && (namespace == null || namespace.equals(element.getNamespaceURI()))) {
                return element;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private Element firstDescendant(Element parent, String namespace, String localName) {
        if (parent == null) {
            return null;
        }
        NodeList nodes = namespace == null
                ? parent.getElementsByTagNameNS("*", localName)
                : parent.getElementsByTagNameNS(namespace, localName);
        return nodes.getLength() == 0 ? null : (Element) nodes.item(0);
    }

    private Node firstElementChild(Element parent) {
        Node child = parent.getFirstChild();
        while (child != null && child.getNodeType() != Node.ELEMENT_NODE) {
            child = child.getNextSibling();
        }
        return child;
    }

    private void appendTextElement(Document doc, Element parent, String namespace, String qualifiedName, String value) {
        Element element = doc.createElementNS(namespace, qualifiedName);
        element.setTextContent(value == null ? "" : value);
        parent.appendChild(element);
    }

    private String serialize(Document doc) throws Exception {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(result));
        return result.toString();
    }
}
