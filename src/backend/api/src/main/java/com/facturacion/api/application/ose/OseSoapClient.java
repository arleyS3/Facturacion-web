package com.facturacion.api.application.ose;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * SOAP client for SUNAT/OSE Webservices operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OseSoapClient {

    private static final String SOAP_ACTION_HEADER = "SOAPAction";
    private static final String SERVICE_NAMESPACE = "http://service.sunat.gob.pe";
    private static final String WSSE_NAMESPACE = "http://docs.oasis-open.org/wss/2004/01/"
            + "oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String DEFAULT_XML_FILENAME = "comprobante.xml";
    private static final Pattern TICKET_PATTERN = Pattern.compile("(?i)ticket\\s*:\\s*(\\d+)");

    private final RestTemplate restTemplate;

    @Value("${ose.endpoint.url}")
    private String endpointUrl;

    @Value("${ose.credentials.username}")
    private String username;

    @Value("${ose.credentials.password}")
    private String password;

    @Value("${ose.soap-action.send-bill:sendBill}")
    private String sendBillSoapAction;

    @Value("${ose.soap-action.get-status:getStatus}")
    private String getStatusSoapAction;

    public String enviarXml(String xmlUbl) {
        return enviarXml(xmlUbl, DEFAULT_XML_FILENAME);
    }

    public String enviarXml(String xmlUbl, String originalFilename) {
        String xmlFilename = normalizeXmlFilename(originalFilename);
        String zipFilename = toZipFilename(xmlFilename);
        String contentFile = zipXmlContent(xmlFilename, xmlUbl);

        log.info("Enviando XML comprimido al OSE (xml={}, zip={})", xmlFilename, zipFilename);
        return sendBill(zipFilename, contentFile);
    }

    public String sendBill(String fileName, String contentFileBase64) {
        String envelope = buildSendBillEnvelope(fileName, contentFileBase64);
        return postSoap(envelope, sendBillSoapAction, List.of("document", "applicationResponse", "content"));
    }

    public String getStatus(String ticket) {
        String envelope = buildGetStatusEnvelope(ticket);
        return postSoap(envelope, getStatusSoapAction, List.of("content", "statusCode", "statusMessage"));
    }

    private String postSoap(String envelope, String soapAction, List<String> preferredResultElements) {
        HttpEntity<String> request = new HttpEntity<>(envelope, soapHeaders(soapAction));

        log.info("Invocando OSE SOAP {} en {}", soapAction, endpointUrl);
        ResponseEntity<String> response = restTemplate.postForEntity(endpointUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error HTTP " + response.getStatusCode() + ": " + response.getBody());
        }

        return extractResult(response.getBody(), preferredResultElements);
    }

    private HttpHeaders soapHeaders(String soapAction) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.set(SOAP_ACTION_HEADER, soapAction);
        return headers;
    }

    String buildSendBillEnvelope(String fileName, String contentFileBase64) {
        return soapEnvelope("""
                  <ser:sendBill>
                    <fileName>%s</fileName>
                    <contentFile>%s</contentFile>
                  </ser:sendBill>
                """.formatted(escapeXml(fileName), escapeXml(contentFileBase64)));
    }

    String buildGetStatusEnvelope(String ticket) {
        return soapEnvelope("""
                  <ser:getStatus>
                    <ticket>%s</ticket>
                  </ser:getStatus>
                """.formatted(escapeXml(ticket)));
    }

    private String soapEnvelope(String body) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                                  xmlns:ser="%s"
                                  xmlns:wsse="%s">
                  <soapenv:Header>
                    <wsse:Security>
                      <wsse:UsernameToken>
                        <wsse:Username>%s</wsse:Username>
                        <wsse:Password>%s</wsse:Password>
                      </wsse:UsernameToken>
                    </wsse:Security>
                  </soapenv:Header>
                  <soapenv:Body>
                %s
                  </soapenv:Body>
                </soapenv:Envelope>
                """.formatted(
                SERVICE_NAMESPACE,
                WSSE_NAMESPACE,
                escapeXml(username),
                escapeXml(password),
                body);
    }

    String extractResult(String soapResponse, List<String> preferredElementNames) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(soapResponse)));

            Optional<String> preferredResult = preferredElementNames.stream()
                    .map(name -> textContentOfFirst(doc, name))
                    .flatMap(Optional::stream)
                    .findFirst();
            if (preferredResult.isPresent()) {
                return preferredResult.get();
            }

            String bodyText = extractBodyText(doc);
            if (preferredElementNames.contains("ticket")) {
                return extractEmbeddedTicket(bodyText).orElse(bodyText);
            }
            return bodyText;
        } catch (Exception e) {
            throw new RuntimeException("Error al parsear respuesta SOAP: " + e.getMessage(), e);
        }
    }

    private Optional<String> textContentOfFirst(Document doc, String localName) {
        NodeList nodes = doc.getElementsByTagNameNS("*", localName);
        if (nodes.getLength() == 0) {
            nodes = doc.getElementsByTagName(localName);
        }
        if (nodes.getLength() == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodes.item(0).getTextContent())
                .map(String::trim)
                .filter(value -> !value.isBlank());
    }

    private Optional<String> extractEmbeddedTicket(String text) {
        var matcher = TICKET_PATTERN.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(matcher.group(1));
    }

    private String extractBodyText(Document doc) {
        NodeList bodyNodes = doc.getElementsByTagNameNS("http://schemas.xmlsoap.org/soap/envelope/", "Body");
        if (bodyNodes.getLength() == 0) {
            throw new RuntimeException("Body SOAP no encontrado en la respuesta");
        }

        Node body = bodyNodes.item(0);
        String content = body.getTextContent();
        if (content == null || content.isBlank()) {
            throw new RuntimeException("Resultado no encontrado en respuesta SOAP");
        }
        return content.trim();
    }

    private String zipXmlContent(String xmlFilename, String xmlUbl) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(output, StandardCharsets.UTF_8)) {
                zip.putNextEntry(new ZipEntry(xmlFilename));
                zip.write(xmlUbl.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error al comprimir XML para OSE: " + e.getMessage(), e);
        }
    }

    private String normalizeXmlFilename(String originalFilename) {
        String filename = originalFilename == null || originalFilename.isBlank()
                ? DEFAULT_XML_FILENAME
                : originalFilename.trim();
        return filename.toLowerCase().endsWith(".xml") ? filename : filename + ".xml";
    }

    private String toZipFilename(String xmlFilename) {
        return xmlFilename.replaceFirst("(?i)\\.xml$", ".ZIP");
    }

    private String escapeXml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
