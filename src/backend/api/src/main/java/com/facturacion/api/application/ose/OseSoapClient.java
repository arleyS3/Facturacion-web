package com.facturacion.api.application.ose;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente SOAP para enviar XML UBL 2.1 al OSE (DBNet / eComprobantes).
 * <p>
 * Envía el XML firmado o sin firmar al endpoint SOAP del OSE usando
 * el m&eacute;todo {@code putCustomerETDLoadXML}.
 * </p>
 */
@Service
public class OseSoapClient {

    private final RestTemplate restTemplate;
    private final String endpointUrl;

    public OseSoapClient(
            @Value("${ose.endpoint.url}") String endpointUrl) {
        this.restTemplate = new RestTemplate();
        this.endpointUrl = endpointUrl;
    }

    /**
     * Env&iacute;a un XML UBL 2.1 al OSE para su validaci&oacute;n y env&iacute;o a SUNAT.
     *
     * @param xmlUbl contenido del XML UBL 2.1
     * @return respuesta del OSE (ticket, CDR o mensaje de error)
     */
    public String enviarXml(String xmlUbl) {
        String soapEnvelope = construirSoapEnvelope(xmlUbl);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        headers.set("SOAPAction", "http://www.dbnet.cl/putCustomerETDLoadXML");

        HttpEntity<String> request = new HttpEntity<>(soapEnvelope, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                endpointUrl, request, String.class);

        return response.getBody();
    }

    /**
     * Construye el envelope SOAP 1.1 con el XML UBL escapado dentro de &lt;lsXML&gt;.
     */
    private String construirSoapEnvelope(String xmlUbl) {
        String escapedXml = xmlUbl
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");

        return """
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
}
