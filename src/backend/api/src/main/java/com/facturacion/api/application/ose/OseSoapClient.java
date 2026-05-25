package com.facturacion.api.application.ose;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.xml.sax.InputSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cliente SOAP para enviar XML UBL 2.1 al OSE (DBNet / eComprobantes).
 * <p>
 * Envía el XML firmado o sin firmar al endpoint SOAP del OSE usando
 * el método {@code putCustomerETDLoadXML}.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OseSoapClient {

    private final RestTemplate restTemplate;
    @Value("${ose.endpoint.url}")
    private String endpointUrl;

    private static final String SOAP_ACTION = "http://www.dbnet.cl/putCustomerETDLoadXML";
    private static final String SOAP_NAMESPACE = "http://www.dbnet.cl";

    public String enviarXml(String xmlUbl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "xml", StandardCharsets.UTF_8));
        headers.set("SOAPAction", SOAP_ACTION);

        HttpEntity<String> request = new HttpEntity<>(construirSoapEnvelope(xmlUbl), headers);

        log.info("Lo que se va a enviar: {}", construirSoapEnvelope(xmlUbl));
        ResponseEntity<String> response = restTemplate.postForEntity(
                endpointUrl, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Error HTTP " + response.getStatusCode()
                    + ": " + response.getBody());
        }

        return extraerResultado(response.getBody());
    }

    private String construirSoapEnvelope(String xmlUbl) {
        // Comentarlo para deg
        String escapedXml = xmlUbl
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");

        return """
                <?xml version="1.0" encoding="utf-8"?>
                <soap:Envelope
                    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                    xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
                  <soap:Body>
                    <putCustomerETDLoadXML xmlns="http://www.dbnet.cl">
                      <lsXML>%s</lsXML>
                    </putCustomerETDLoadXML>
                  </soap:Body>
                </soap:Envelope>
                """.formatted(escapedXml);
    }

    private String extraerResultado(String soapResponse) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            Document doc = factory.newDocumentBuilder()
                    .parse(new InputSource(new StringReader(soapResponse)));

            NodeList nodes = doc.getElementsByTagNameNS(
                    SOAP_NAMESPACE, "putCustomerETDLoadXMLResult");

            if (nodes.getLength() > 0) {
                return nodes.item(0).getTextContent();
            }

            throw new RuntimeException("Resultado no encontrado en respuesta SOAP");

        } catch (Exception e) {
            throw new RuntimeException("Error al parsear respuesta SOAP: " + e.getMessage(), e);
        }
    }
}
