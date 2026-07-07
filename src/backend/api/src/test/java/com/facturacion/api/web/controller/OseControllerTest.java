package com.facturacion.api.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.facturacion.api.application.comprobante.ubl.signature.XmlSignatureService;
import com.facturacion.api.application.ose.OseSoapClient;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class OseControllerTest {

    private OseSoapClient oseSoapClient;
    private XmlSignatureService xmlSignatureService;
    private OseController controller;

    @BeforeEach
    void setUp() {
        oseSoapClient = mock(OseSoapClient.class);
        xmlSignatureService = mock(XmlSignatureService.class);
        controller = new OseController(oseSoapClient, xmlSignatureService);
    }

    @Test
    void enviarXmlSignsXmlUsingRucFromFilenameBeforeSendingToOse() throws Exception {
        String xml = unsignedInvoice("20100119065");
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "20100119065-01-F123-00012506.xml",
                "application/xml",
                xml.getBytes(StandardCharsets.UTF_8));
        when(xmlSignatureService.signXml(xml, "20100119065")).thenReturn("<signed/>");
        when(oseSoapClient.enviarXml("<signed/>", "20100119065-01-F123-00012506.xml")).thenReturn("OK");

        var response = controller.enviarXml(file);

        assertThat(response).containsEntry("success", true);
        assertThat(response).containsEntry("resultado", "OK");
        verify(xmlSignatureService).signXml(xml, "20100119065");
        verify(oseSoapClient).enviarXml("<signed/>", "20100119065-01-F123-00012506.xml");
    }

    @Test
    void enviarXmlFallsBackToSupplierRucFromXmlWhenFilenameDoesNotMatchSunatFormat() throws Exception {
        String xml = unsignedInvoice("20100119065");
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "invoice.xml",
                "application/xml",
                xml.getBytes(StandardCharsets.UTF_8));
        when(xmlSignatureService.signXml(xml, "20100119065")).thenReturn("<signed/>");
        when(oseSoapClient.enviarXml("<signed/>", "20100119065-01-F123-00012506.xml")).thenReturn("OK");

        var response = controller.enviarXml(file);

        assertThat(response).containsEntry("success", true);
        verify(xmlSignatureService).signXml(xml, "20100119065");
        verify(oseSoapClient).enviarXml("<signed/>", "20100119065-01-F123-00012506.xml");
    }

    @Test
    void enviarXmlFailsWhenCertificateIsNotConfigured() throws Exception {
        String xml = unsignedInvoice("20100119065");
        MockMultipartFile file = new MockMultipartFile(
                "archivo",
                "20100119065-01-F123-00012506.xml",
                "application/xml",
                xml.getBytes(StandardCharsets.UTF_8));
        when(xmlSignatureService.signXml(xml, "20100119065"))
                .thenThrow(new IllegalStateException("No hay certificado configurado para RUC: 20100119065"));

        var response = controller.enviarXml(file);

        assertThat(response).containsEntry("success", false);
        assertThat(response.get("error").toString()).contains("No hay certificado configurado para RUC: 20100119065");
    }

    private String unsignedInvoice(String ruc) {
        return """
                <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                         xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                         xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2">
                  <cbc:ID>F123-00012506</cbc:ID>
                  <cbc:InvoiceTypeCode>01</cbc:InvoiceTypeCode>
                  <cac:AccountingSupplierParty>
                    <cac:Party>
                      <cac:PartyLegalEntity>
                        <cbc:CompanyID>%s</cbc:CompanyID>
                      </cac:PartyLegalEntity>
                    </cac:Party>
                  </cac:AccountingSupplierParty>
                </Invoice>
                """.formatted(ruc);
    }
}
