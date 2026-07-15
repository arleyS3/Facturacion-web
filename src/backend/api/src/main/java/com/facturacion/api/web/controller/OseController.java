package com.facturacion.api.web.controller;

import com.facturacion.api.application.comprobante.ubl.signature.XmlSignatureService;
import com.facturacion.api.application.ose.OseSoapClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * OSE endpoints for SUNAT SOAP Webservices operations.
 */
@RestController
@RequestMapping("/api/v1/ose")
@Tag(name = "OSE", description = "Envío de XML/ZIP al Operador de Servicios Electrónicos")
@Slf4j
public class OseController {

    private static final Pattern SUNAT_XML_FILENAME_PATTERN = Pattern.compile(
            "^(\\d{11})-[A-Z0-9]{2}-[A-Z0-9]+-[A-Z0-9]+\\.xml$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern SUNAT_ZIP_FILENAME_PATTERN = Pattern.compile(
            "^(\\d{11})-[A-Z0-9]{2}-[A-Z0-9]+-[A-Z0-9]+\\.zip$",
            Pattern.CASE_INSENSITIVE);

    private final OseSoapClient oseSoapClient;
    private final XmlSignatureService xmlSignatureService;

    public OseController(OseSoapClient oseSoapClient, XmlSignatureService xmlSignatureService) {
        this.oseSoapClient = oseSoapClient;
        this.xmlSignatureService = xmlSignatureService;
    }

    @Operation(
            summary = "Enviar XML al OSE",
            description = "Recibe un archivo XML UBL 2.1, lo firma, lo comprime en ZIP y llama a sendBill.")
    @PostMapping(value = "/enviar-xml", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> enviarXml(@RequestParam("archivo") MultipartFile archivo) {
        try {
            String xmlContent = new String(archivo.getBytes(), StandardCharsets.UTF_8);
            String originalFilename = archivo.getOriginalFilename();
            String oseXmlFilename = resolveOseXmlFilename(originalFilename, xmlContent);
            String rucEmisor = rucFromFilename(oseXmlFilename);
            log.info(
                    "Firmando y enviando XML al OSE ({} bytes, nombre={}, nombreOse={}, ruc={})",
                    xmlContent.length(),
                    originalFilename,
                    oseXmlFilename,
                    rucEmisor);

            String signedXml = xmlSignatureService.signXml(xmlContent, rucEmisor);
            String respuestaOse = oseSoapClient.enviarXml(signedXml, oseXmlFilename);
            return success(respuestaOse);
        } catch (IOException e) {
            log.error("Error al leer el archivo XML: {}", e.getMessage(), e);
            return failure("Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error al enviar XML al OSE: {}", e.getMessage(), e);
            return failure("Error al enviar XML al OSE: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Enviar ZIP al OSE con sendBill",
            description = "Recibe el ZIP SUNAT del comprobante y lo envía al OSE usando sendBill.")
    @PostMapping(value = "/send-bill", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> sendBill(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam(value = "fileName", required = false) String fileName) {
        try {
            String resolvedFileName = resolveZipFileName(fileName, archivo.getOriginalFilename());
            String contentFile = Base64.getEncoder().encodeToString(archivo.getBytes());
            log.info("Enviando ZIP al OSE con sendBill ({} bytes, nombre={})", archivo.getSize(), resolvedFileName);

            String respuestaOse = oseSoapClient.sendBill(resolvedFileName, contentFile);
            return success(respuestaOse);
        } catch (IOException e) {
            log.error("Error al leer el archivo ZIP: {}", e.getMessage(), e);
            return failure("Error al leer el archivo: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error en sendBill OSE: {}", e.getMessage(), e);
            return failure("Error en sendBill OSE: " + e.getMessage());
        }
    }

    @Operation(summary = "Consultar estado OSE", description = "Consulta el estado de un ticket con getStatus.")
    @GetMapping("/status/{ticket}")
    public Map<String, Object> getStatus(@PathVariable String ticket) {
        try {
            log.info("Consultando estado OSE para ticket {}", ticket);
            String respuestaOse = oseSoapClient.getStatus(ticket);
            return success(respuestaOse);
        } catch (Exception e) {
            log.error("Error en getStatus OSE: {}", e.getMessage(), e);
            return failure("Error en getStatus OSE: " + e.getMessage());
        }
    }

    private Map<String, Object> success(String resultado) {
        return Map.of(
                "success", true,
                "resultado", resultado);
    }

    private Map<String, Object> failure(String error) {
        return Map.of(
                "success", false,
                "error", error);
    }

    private String resolveOseXmlFilename(String filename, String xmlContent) throws Exception {
        if (isValidSunatXmlFilename(filename)) {
            return filename.trim();
        }

            XmlDocumentInfo info = extractXmlDocumentInfo(xmlContent);
        if (info.isComplete()) {
            return "%s-%s-%s.xml".formatted(
                    info.rucEmisor(),
                    info.tipoDocumento().toUpperCase(),
                    info.numeroDocumento().toUpperCase());
        }

        throw new IllegalArgumentException(
                "No se pudo construir el nombre SUNAT del XML. Formato requerido: RUC-TIPO-SERIE-CORRELATIVO.xml");
    }

    private boolean isValidSunatXmlFilename(String filename) {
        return filename != null && SUNAT_XML_FILENAME_PATTERN.matcher(filename.trim()).matches();
    }

    private String rucFromFilename(String filename) {
        var matcher = SUNAT_XML_FILENAME_PATTERN.matcher(filename.trim());
        return matcher.matches() ? matcher.group(1) : "";
    }

    private XmlDocumentInfo extractXmlDocumentInfo(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        var doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xmlContent)));
        NodeList supplierNodes = doc.getElementsByTagNameNS("*", "AccountingSupplierParty");
        String ruc = "";
        if (supplierNodes.getLength() > 0) {
            Element supplier = (Element) supplierNodes.item(0);
            ruc = firstMatchingRuc(supplier, "ID");
            if (ruc.isBlank()) {
                ruc = firstMatchingRuc(supplier, "CompanyID");
            }
        }

        String tipoDocumento = firstText(doc.getElementsByTagNameNS("*", "InvoiceTypeCode"));
        if (tipoDocumento.isBlank()) {
            tipoDocumento = firstText(doc.getElementsByTagNameNS("*", "CreditNoteTypeCode"));
        }
        if (tipoDocumento.isBlank()) {
            tipoDocumento = firstText(doc.getElementsByTagNameNS("*", "DebitNoteTypeCode"));
        }

        String numeroDocumento = firstDirectDocumentId((Element) doc.getDocumentElement());
        return new XmlDocumentInfo(ruc, tipoDocumento, numeroDocumento);
    }

    private String firstMatchingRuc(Element root, String localName) {
        NodeList nodes = root.getElementsByTagNameNS("*", localName);
        for (int index = 0; index < nodes.getLength(); index++) {
            String value = nodes.item(index).getTextContent();
            if (value != null && value.trim().matches("\\d{11}")) {
                return value.trim();
            }
        }
        return "";
    }

    private String firstText(NodeList nodes) {
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent() == null) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
    }

    private String firstDirectDocumentId(Element root) {
        NodeList nodes = root.getElementsByTagNameNS("*", "ID");
        for (int index = 0; index < nodes.getLength(); index++) {
            if (nodes.item(index).getParentNode() == root) {
                return nodes.item(index).getTextContent().trim();
            }
        }
        return "";
    }

    private record XmlDocumentInfo(String rucEmisor, String tipoDocumento, String numeroDocumento) {
        boolean isComplete() {
            return rucEmisor != null && rucEmisor.matches("\\d{11}")
                    && tipoDocumento != null && tipoDocumento.matches("[A-Z0-9]{2}")
                    && numeroDocumento != null && numeroDocumento.matches("[A-Z0-9]+-[A-Z0-9]+");
        }
    }

    private String resolveZipFileName(String explicitFileName, String uploadedFileName) {
        String selectedFileName = explicitFileName != null && !explicitFileName.isBlank()
                ? explicitFileName.trim()
                : uploadedFileName;

        if (selectedFileName == null || selectedFileName.isBlank()) {
            throw new IllegalArgumentException("El nombre del ZIP es requerido para sendBill");
        }
        if (!SUNAT_ZIP_FILENAME_PATTERN.matcher(selectedFileName).matches()) {
            throw new IllegalArgumentException(
                    "El archivo para sendBill debe usar formato RUC-TIPO-SERIE-CORRELATIVO.ZIP");
        }
        return selectedFileName;
    }
}
