package com.facturacion.api.application;

import jakarta.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utilidad para encriptar/desencriptar datos sensibles.
 * 
 * <p>
 * Usa AES-256 para encriptar contraseñas y datos sensibles antes de
 * guardarlos en la BD.
 * </p>
 * 
 * <p>
 * La clave de encriptación se configura en application.yaml:
 * <pre>
 * app:
 *   encryption:
 *     key: "clave-secreta-32-caracteres-aqui"
 * </pre>
 * </p>
 */
@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    
    @Value("${app.encryption.key}")
    private String encryptionKey;

    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        // Asegurar que la clave tenga exactamente 32 bytes para AES-256
        String key32 = encryptionKey;
        if (key32.length() < 32) {
            key32 = String.format("%-32s", key32).replace(' ', 'X');
        } else if (key32.length() > 32) {
            key32 = key32.substring(0, 32);
        }
        this.secretKey = new SecretKeySpec(key32.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    /**
     * Encripta un texto plano.
     *
     * @param text texto a encriptar
     * @return texto encriptado en Base64
     * @throws Exception si falla la encriptación
     */
    public String encrypt(String text) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encrypted = cipher.doFinal(text.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * Desencripta un texto previamente encriptado.
     *
     * @param encryptedText texto encriptado en Base64
     * @return texto desencriptado
     * @throws Exception si falla la desencriptación
     */
    public String decrypt(String encryptedText) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}