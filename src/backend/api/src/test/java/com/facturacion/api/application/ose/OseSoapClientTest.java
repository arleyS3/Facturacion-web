package com.facturacion.api.application.ose;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

class OseSoapClientTest {

    private RestTemplate restTemplate;
    private OseSoapClient client;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        client = new OseSoapClient(restTemplate);
        ReflectionTestUtils.setField(client, "endpointUrl", "https://ose.test/ws");
        ReflectionTestUtils.setField(client, "username", "20100119065MODDATOS");
        ReflectionTestUtils.setField(client, "password", "moddatos");
        ReflectionTestUtils.setField(client, "sendBillSoapAction", "sendBill");
        ReflectionTestUtils.setField(client, "getStatusSoapAction", "getStatus");
    }

    @Test
    void sendBillBuildsSunatEnvelopeWithWsSecurityAndZipPayload() {
        String response = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <sendBillResponse xmlns="http://service.sunat.gob.pe">
                      <applicationResponse>APPLICATION_RESPONSE_BASE64</applicationResponse>
                    </sendBillResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """;
        ArgumentCaptor<HttpEntity<String>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq("https://ose.test/ws"), requestCaptor.capture(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        String result = client.sendBill("20100119065-01-F123-00012506.ZIP", "UEsDBAoAAAAAA");

        assertThat(result).isEqualTo("APPLICATION_RESPONSE_BASE64");
        String envelope = requestCaptor.getValue().getBody();
        assertThat(envelope).contains("xmlns:ser=\"http://service.sunat.gob.pe\"");
        assertThat(envelope).contains("xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/");
        assertThat(envelope).contains("<wsse:Username>20100119065MODDATOS</wsse:Username>");
        assertThat(envelope).contains("<wsse:Password>moddatos</wsse:Password>");
        assertThat(envelope).contains("<ser:sendBill>");
        assertThat(envelope).contains("<fileName>20100119065-01-F123-00012506.ZIP</fileName>");
        assertThat(envelope).contains("<contentFile>UEsDBAoAAAAAA</contentFile>");
        assertThat(envelope).doesNotContain("putCustomerETDLoadXML");
        assertThat(envelope).doesNotContain("http://www.dbnet.cl");
        assertThat(requestCaptor.getValue().getHeaders().getFirst("SOAPAction")).isEqualTo("sendBill");
    }

    @Test
    void sendBillFailsClearlyWhenOseConfigurationIsMissing() {
        ReflectionTestUtils.setField(client, "endpointUrl", "");
        ReflectionTestUtils.setField(client, "username", "");
        ReflectionTestUtils.setField(client, "password", "");

        assertThatThrownBy(() -> client.sendBill("20100119065-01-F123-00012506.ZIP", "UEsDBAoAAAAAA"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("OSE SOAP configuration is incomplete. Set OSE_ENDPOINT_URL, OSE_USERNAME, and OSE_PASSWORD.");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void sendBillFailsBeforeHttpCallWhenSoapActionIsBlank() {
        ReflectionTestUtils.setField(client, "sendBillSoapAction", "");

        assertThatThrownBy(() -> client.sendBill("20100119065-01-F123-00012506.ZIP", "UEsDBAoAAAAAA"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("OSE SOAPAction configuration is incomplete. Set OSE_SOAP_ACTION_SEND_BILL and OSE_SOAP_ACTION_GET_STATUS.");
        verifyNoInteractions(restTemplate);
    }

    @Test
    void getStatusBuildsEnvelopeWithTicket() {
        String response = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <getStatusResponse xmlns="http://service.sunat.gob.pe">
                      <status>
                        <statusCode>0</statusCode>
                        <statusMessage>Procesado correctamente</statusMessage>
                      </status>
                    </getStatusResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """;
        ArgumentCaptor<HttpEntity<String>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        when(restTemplate.postForEntity(eq("https://ose.test/ws"), requestCaptor.capture(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(response, HttpStatus.OK));

        String result = client.getStatus("20190000000000001936649");

        assertThat(result).isEqualTo("0");
        String envelope = requestCaptor.getValue().getBody();
        assertThat(envelope).contains("<ser:getStatus>");
        assertThat(envelope).contains("<ticket>20190000000000001936649</ticket>");
        assertThat(requestCaptor.getValue().getHeaders().getFirst("SOAPAction")).isEqualTo("getStatus");
    }

    @Test
    void extractResultReturnsFullFaultTextWhenSendBillRespondsWithEmbeddedTicket() {
        String response = """
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                  <s:Body>
                    <s:Fault>
                      <faultcode>s:Client</faultcode>
                      <faultstring>100-El sistema no puede responder su solicitud. Detalle: ' ticket: 20260000000000221669964 error: Error en Procesamiento '</faultstring>
                    </s:Fault>
                  </s:Body>
                </s:Envelope>
                """;

        String result = client.extractResult(response, List.of("document", "applicationResponse", "content"));

        assertThat(result).contains("ticket: 20260000000000221669964");
        assertThat(result).contains("Error en Procesamiento");
    }

    @Test
    void extractResultReturnsEmbeddedTicketWhenGetStatusUsesTicketPreference() {
        String response = """
                <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/">
                  <s:Body>
                    <s:Fault>
                      <faultcode>s:Client</faultcode>
                      <faultstring>Detalle: ' ticket: 20260000000000221669964 error: Error en Procesamiento '</faultstring>
                    </s:Fault>
                  </s:Body>
                </s:Envelope>
                """;

        String result = client.extractResult(response, List.of("ticket"));

        assertThat(result).isEqualTo("20260000000000221669964");
    }

    @Test
    void extractResultFallsBackToBodyTextWhenPreferredElementsAreAbsent() {
        String response = """
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
                  <soapenv:Body>
                    <customResponse>OK</customResponse>
                  </soapenv:Body>
                </soapenv:Envelope>
                """;

        String result = client.extractResult(response, List.of("missingElement"));

        assertThat(result).isEqualTo("OK");
    }
}
