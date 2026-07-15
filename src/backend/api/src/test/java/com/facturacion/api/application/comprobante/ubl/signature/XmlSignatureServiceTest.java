package com.facturacion.api.application.comprobante.ubl.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.facturacion.api.application.EncryptionUtil;
import com.facturacion.api.web.repositories.ConfiguracionCertificadoRepository;
import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import javax.security.auth.x500.X500Principal;
import org.junit.jupiter.api.Test;

class XmlSignatureServiceTest {

    @Test
    void signXmlWithKeyAddsUblExtensionAndCacSignatureReference() throws Exception {
        XmlSignatureService service = new XmlSignatureService(
                mock(ConfiguracionCertificadoRepository.class),
                mock(EncryptionUtil.class));
        var keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        X509Certificate certificate = mockCertificate();

        String signedXml = service.signXmlWithKey(unsignedInvoice(), keyPair.getPrivate(), certificate);

        assertThat(signedXml).contains("<ext:UBLExtensions");
        assertThat(signedXml).contains("<ext:ExtensionContent>");
        assertThat(signedXml).contains("<ds:Signature");
        assertThat(signedXml).contains("Id=\"SF123-00012506\"");
        assertThat(signedXml).contains("<cac:Signature>");
        assertThat(signedXml).contains("<cbc:URI>#SF123-00012506</cbc:URI>");
        assertThat(signedXml).contains("<ds:SignatureValue>");
        assertThat(signedXml).contains("<ds:X509Certificate>");
    }

    private X509Certificate mockCertificate() throws Exception {
        X509Certificate certificate = mock(X509Certificate.class);
        when(certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=Test Certificate"));
        when(certificate.getSerialNumber()).thenReturn(BigInteger.ONE);
        when(certificate.getNotBefore()).thenReturn(Date.from(Instant.parse("2026-01-01T00:00:00Z")));
        when(certificate.getNotAfter()).thenReturn(Date.from(Instant.parse("2027-01-01T00:00:00Z")));
        when(certificate.getEncoded()).thenReturn("test-certificate".getBytes());
        return certificate;
    }

    private String unsignedInvoice() {
        return """
                <Invoice xmlns="urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
                         xmlns:cac="urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2"
                         xmlns:cbc="urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2"
                         xmlns:ext="urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2">
                  <cbc:UBLVersionID>2.1</cbc:UBLVersionID>
                  <cbc:CustomizationID>2.0</cbc:CustomizationID>
                  <cbc:ID>F123-00012506</cbc:ID>
                  <cac:AccountingSupplierParty>
                    <cac:Party>
                      <cac:PartyIdentification>
                        <cbc:ID>20100119065</cbc:ID>
                      </cac:PartyIdentification>
                      <cac:PartyLegalEntity>
                        <cbc:RegistrationName>TEST COMPANY</cbc:RegistrationName>
                      </cac:PartyLegalEntity>
                    </cac:Party>
                  </cac:AccountingSupplierParty>
                </Invoice>
                """;
    }
}
