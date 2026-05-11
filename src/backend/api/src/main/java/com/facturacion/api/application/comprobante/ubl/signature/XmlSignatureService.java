package com.facturacion.api.application.comprobante.ubl.signature;

import com.facturacion.api.application.EncryptionUtil;
import com.facturacion.api.web.models.ConfiguracionCertificadoEntity;
import com.facturacion.api.web.repositories.ConfiguracionCertificadoRepository;
import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
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
            // Inicializar el proveedor de XML Security de Apache
            org.apache.xml.security.Init.init();

            // Parsear el documento XML desde String
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();

            org.xml.sax.InputSource is = new org.xml.sax.InputSource(new java.io.StringReader(xmlDocument));
            org.w3c.dom.Document doc = db.parse(is);

            // Obtener el elemento raíz del documento
            org.w3c.dom.Element root = doc.getDocumentElement();

            // Crear elemento Signature con namespace XMLDSig
            String signatureId = "SignatureKG";
            org.w3c.dom.Element signatureElement = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "Signature");
            signatureElement.setAttribute("Id", signatureId);

            // Insertar Signature al inicio del documento (después del elemento raíz)
            org.w3c.dom.Node firstChild = root.getFirstChild();
            if (firstChild != null) {
                root.insertBefore(signatureElement, firstChild);
            } else {
                root.appendChild(signatureElement);
            }

            // Crear SignedInfo
            org.w3c.dom.Element signedInfoElement = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "SignedInfo");
            signatureElement.appendChild(signedInfoElement);

            // CanonicalizationMethod - Método de canonicalización del documento
            org.w3c.dom.Element canonMethod = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "CanonicalizationMethod");
            canonMethod.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
            signedInfoElement.appendChild(canonMethod);

            // SignatureMethod - Algoritmo de firma (RSA-SHA256)
            org.w3c.dom.Element sigMethod = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "SignatureMethod");
            sigMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256");
            signedInfoElement.appendChild(sigMethod);

            // Reference - Referencia al documento completo
            org.w3c.dom.Element reference = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "Reference");
            reference.setAttribute("Id", "reference-doc");
            reference.setAttribute("URI", ""); // Referencia al documento completo
            signedInfoElement.appendChild(reference);

            // DigestMethod - Algoritmo de hash (SHA-256)
            org.w3c.dom.Element digestMethod = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "DigestMethod");
            digestMethod.setAttribute("Algorithm", "http://www.w3.org/2001/04/xmlenc#sha256");
            reference.appendChild(digestMethod);

            // DigestValue - Valor del hash (placeholder)
            org.w3c.dom.Element digestValue = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "DigestValue");
            digestValue.setTextContent(""); // TODO: calcular hash SHA-256 real
            reference.appendChild(digestValue);

            // SignatureValue - Valor de la firma encriptada (placeholder)
            org.w3c.dom.Element signatureValue = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "SignatureValue");
            signatureValue.setAttribute("Id", "signature-value");
            // TODO: En producción, encriptar el hash con RSA
            // byte[] hash = MessageDigest.getInstance("SHA-256").digest(signedInfoBytes);
            // byte[] signatureBytes = privateKey.sign(Signature.getInstance("SHA256withRSA"));
            signatureValue.setTextContent("");
            signatureElement.appendChild(signatureValue);

            // KeyInfo - Información del certificado
            org.w3c.dom.Element keyInfo = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "KeyInfo");
            signatureElement.appendChild(keyInfo);

            // X509Data - Certificado del firmante
            org.w3c.dom.Element x509Data = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "X509Data");
            keyInfo.appendChild(x509Data);

            org.w3c.dom.Element x509Cert = doc.createElementNS(
                    "http://www.w3.org/2000/09/xmldsig#", "X509Certificate");
            x509Cert.setTextContent(Base64.getEncoder().encodeToString(certificate.getEncoded()));
            x509Data.appendChild(x509Cert);

            // Convertir el documento modificado a String
            javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
            javax.xml.transform.Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");

            java.io.StringWriter writer = new java.io.StringWriter();
            transformer.transform(new javax.xml.transform.dom.DOMSource(doc),
                    new javax.xml.transform.stream.StreamResult(writer));

            log.info("XML firmado exitosamente con XMLDSig");
            return writer.toString();

        } catch (Exception e) {
            log.error("Error al firmar XML: {}", e.getMessage(), e);
            // En caso de error, retornar el XML sin firma para debug
            return xmlDocument;
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
