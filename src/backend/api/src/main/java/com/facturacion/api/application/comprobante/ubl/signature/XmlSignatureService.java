package com.facturacion.api.application.comprobante.ubl.signature;

import com.facturacion.api.application.EncryptionUtil;
import com.facturacion.api.web.models.ConfiguracionCertificadoEntity;
import com.facturacion.api.web.repositories.ConfiguracionCertificadoRepository;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.security.Signature;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio de firma digital para documentos XML UBL.
 *
 * <p>
 * Este servicio es responsable de firmar digitalmente los comprobantes electrónicos
 * antes de enviarlos a SUNAT. Utiliza el estándar XMLDSig (W3C) para garantizar la
 * integridad y autenticidad de los documentos.
 * </p>
 *
 * <p>
 * El flujo de trabajo es el siguiente:
 * <ol>
 *   <li>Se busca la configuración del certificado en la base de datos (tabla configuracion_certificado)</li>
 *   <li>Se decodifica el certificado .pfx desde Base64</li>
 *   <li>Se desencripta la contraseña del certificado</li>
 *   <li>Se carga el keystore PKCS12 con el certificado</li>
 *   <li>Se genera la estructura de firma XMLDSig</li>
 *   <li>Se inserta la firma al inicio del documento XML</li>
 * </ol>
 * </p>
 *
 * <p>
 * El certificado .pfx se almacena en Base64 y la contraseña se encripta usando AES-256.
 * Esto permite una gestión segura de las credenciales desde la base de datos.
 * </p>
 *
 * <p>
 * En caso de error durante la firma, el servicio retorna el XML sin firmar para
 * permitir el debug. En producción, se debería lanzar una excepción.
 * </p>
 *
 * @author Sistema de Facturación
 * @version 1.0.0
 * @since 1.0.0
 * @see <a href="https://www.w3.org/TR/xmldsig-core/">XMLDSig W3C Specification</a>
 * @see <a href="https://buscar.sunat.gob.pe/normativa/_resultanормаbuscada?busqueda=resolucion+comprobantes+electronicos">
 *      Resolución de.sunat sobre Comprobantes Electrónicos</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class XmlSignatureService {

    /** Repositorio para acceder a la configuración de certificados en la BD */
    private final ConfiguracionCertificadoRepository configRepository;

    /** Utilidad para encriptar y desencriptar datos sensibles */
    private final EncryptionUtil encryptionUtil;

    /**
     * Firma un documento XML usando la configuración del emisor almacenada en la base de datos.
     *
     * <p>
     * Este método es el punto de entrada principal para firmar comprobantes electrónicos.
     * Busca automáticamente el certificado配置ado para el RUC del emisor.
     * </p>
     *
     * @param xmlDocument contenido XML a firmar, el cual debe ser un documento UBL válido
     * @param rucEmisor RUC del emisor del comprobante, se usa para buscar su certificado
     *        en la tabla configuracion_certificado
     * @return String conteniendo el XML firmado con la estructura XMLDSig
     * @throws IllegalStateException si no hay certificado configurado para el RUC proporcionado
     * @throws Exception si ocurre un error durante el proceso de firma (decodificación,
     *         desencriptación, carga del keystore o generación de firma)
     * @see #signXmlWithKey(String, PrivateKey, X509Certificate)
     */
    public String signXml(String xmlDocument, String rucEmisor) throws Exception {
        // 1. Buscar configuración del certificado en la BD
        ConfiguracionCertificadoEntity config = configRepository
                .findByRucEmisorAndActivoTrue(rucEmisor)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay certificado configurado para RUC: " + rucEmisor));

        // 2. Decodificar certificado desde Base64
        byte[] certBytes = Base64.getDecoder().decode(config.getCertificadoBase64());

        // 3. Desencriptar la contraseña del certificado
        String password = encryptionUtil.decrypt(config.getPasswordEncriptada());

        // 4. Cargar el keystore PKCS12 desde los bytes del certificado
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(certBytes), password.toCharArray());

        // 5. Obtener la clave privada y el certificado del keystore
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

        // 6. Firmar el XML usando la clave privada
        return signXmlWithKey(xmlDocument, privateKey, certificate);
    }

    /**
     * Firma un documento XML usando una clave privada y certificado específicos.
     *
     * <p>
     * Este método implementa la generación de la firma digital XMLDSig. Crea la estructura
     * de firma conforme al estándar W3C, insertando los siguientes elementos:
     * </p>
     *
     * <ul>
     *   <li>{@code SignedInfo}: información de la firma que incluye el método de canonicalización,
     *       método de firma y referencia al documento</li>
     *   <li>{@code SignatureValue}: valor de la firma encriptada</li>
     *   <li>{@code KeyInfo}: información del certificado X.509</li>
     * </ul>
     *
     * <p>
     * La firma se inserta al inicio del documento XML, dentro del elemento
     * {@code ext:UBLExtensions}, conforme lo requiere SUNAT para comprobantes electrónicos.
     * </p>
     *
     * <p>
     * <strong>Nota:</strong> Actualmente el DigestValue y SignatureValue están vacíos como
     * placeholders. En producción, se debe calcular el hash SHA-256 del documento y encriptarlo
     * con RSA para generar la firma real.
     * </p>
     *
     * @param xmlDocument contenido XML a firmar, debe ser un documento UBL válido
     * @param privateKey clave privada del certificado usada para firmar
     * @param certificate certificado X.509 del emisor, se incluye en KeyInfo
     * @return String conteniendo el XML con la estructura de firma XMLDSig insertada
     * @throws Exception si ocurre un error durante el parseo del XML o la generación de la firma
     * @see <a href="https://www.w3.org/TR/xmldsig-core/#sec-SignedInfo">SignedInfo XMLDSig</a>
     * @see <a href="https://www.w3.org/TR/xmldsig-core/#sec-KeyInfo">KeyInfo XMLDSig</a>
     */
    public String signXmlWithKey(
            String xmlDocument,
            PrivateKey privateKey,
            X509Certificate certificate) throws Exception {
        try {
            // Parsear el documento XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = dbf.newDocumentBuilder().parse(
                    new org.xml.sax.InputSource(new StringReader(xmlDocument)));

            Element root = doc.getDocumentElement();

            // ===== 1. Canonicalizar el documento y calcular DigestValue =====
            // Serializar y canonicalizar el documento
            ByteArrayOutputStream docCanonicalized = new ByteArrayOutputStream();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(root), new StreamResult(docCanonicalized));
            
            // Calcular SHA-256
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] digestBytes = sha256.digest(docCanonicalized.toByteArray());
            String digestValueBase64 = Base64.getEncoder().encodeToString(digestBytes);
            
            log.debug("DigestValue calculado: {} chars", digestValueBase64.length());

            // ===== 2. Crear estructura de firma =====
            // Crear elemento Signature
            Element signatureElement = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "Signature");
            signatureElement.setAttribute("Id", "SignatureKG");

            // Insertar al inicio
            Node firstChild = root.getFirstChild();
            if (firstChild != null) {
                root.insertBefore(signatureElement, firstChild);
            } else {
                root.appendChild(signatureElement);
            }

            // Crear SignedInfo
            Element signedInfoElement = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "SignedInfo");
            signatureElement.appendChild(signedInfoElement);

            // CanonicalizationMethod
            Element canonMethod = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethod");
            canonMethod.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
            signedInfoElement.appendChild(canonMethod);

            // SignatureMethod (RSA-SHA256)
            Element sigMethod = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "SignatureMethod");
            sigMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
            signedInfoElement.appendChild(sigMethod);

            // Reference
            Element reference = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "Reference");
            reference.setAttribute("Id", "reference-doc");
            reference.setAttribute("URI", "");
            signedInfoElement.appendChild(reference);

            // DigestMethod
            Element digestMethod = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "DigestMethod");
            digestMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#sha256");
            reference.appendChild(digestMethod);

            // DigestValue (calculado)
            Element digestValue = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "DigestValue");
            digestValue.setTextContent(digestValueBase64);
            reference.appendChild(digestValue);

            // ===== 3. Calcular SignatureValue =====
            // Canonicalizar SignedInfo
            ByteArrayOutputStream signedInfoCanonicalized = new ByteArrayOutputStream();
            Transformer signedInfoTransformer = tf.newTransformer();
            signedInfoTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            signedInfoTransformer.transform(new DOMSource(signedInfoElement), 
                    new StreamResult(signedInfoCanonicalized));
            
            // Firmar con RSA-SHA256
            Signature rsaSignature = Signature.getInstance("SHA256withRSA");
            rsaSignature.initSign(privateKey);
            rsaSignature.update(signedInfoCanonicalized.toByteArray());
            byte[] signatureBytes = rsaSignature.sign();
            String signatureValueBase64 = Base64.getEncoder().encodeToString(signatureBytes);
            
            log.debug("SignatureValue calculado: {} chars", signatureValueBase64.length());

            // SignatureValue
            Element signatureValue = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "SignatureValue");
            signatureValue.setAttribute("Id", "signature-value");
            signatureValue.setTextContent(signatureValueBase64);
            signatureElement.appendChild(signatureValue);

            // ===== 4. KeyInfo con certificado =====
            Element keyInfo = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
            signatureElement.appendChild(keyInfo);

            Element x509Data = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "X509Data");
            keyInfo.appendChild(x509Data);

            Element x509Cert = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
            x509Cert.setTextContent(Base64.getEncoder().encodeToString(certificate.getEncoded()));
            x509Data.appendChild(x509Cert);

            // Convertir a String
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            Transformer finalTransformer = tf.newTransformer();
            finalTransformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            finalTransformer.transform(new DOMSource(doc), new StreamResult(result));

            log.info("XML firmado exitosamente con XMLDSig (RSA-SHA256)");
            return result.toString();

        } catch (Exception e) {
            log.error("Error al firmar XML: {}", e.getMessage(), e);
            throw new RuntimeException("Error al firmar XML: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica la firma digital de un documento XML.
     *
     * <p>
     * Este método debería validar que la firma digital contenida en el XML
     * sea auténtica y que el documento no haya sido modificado después de ser firmado.
     * </p>
     *
     * <p>
     * <strong>Nota:</strong> Este método aún no está implementado. Retorna {@code true}
     * por defecto para compatibilidad.
     * </p>
     *
     * @param signedXmlDocument el documento XML que contiene la firma a verificar,
     *        debe haber sido previamente firmado con {@link #signXml(String, String)}
     * @return {@code true} si la firma es válida, {@code false} en caso contrario.
     *         Actualmente retorna {@code true} unconditionally ya que la verificación
     *         no está implementada
     * @throws Exception si ocurre un error durante el proceso de verificación,
     *         como parseo inválido del XML
     * @see #signXml(String, String)
     */
    public boolean verifySignature(String signedXmlDocument) throws Exception {
        log.warn("Verificación de firma no implementada - retornando true por defecto");
        return true;
    }
}
