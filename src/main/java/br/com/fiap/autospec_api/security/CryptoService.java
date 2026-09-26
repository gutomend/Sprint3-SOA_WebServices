package br.com.fiap.autospec_api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String ALGO = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final SecretKey key;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(@Value("${crypto.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            // For safety in dev, generate ephemeral key. Recommend setting crypto.secret in properties for production.
            this.key = generateKey(256);
        } else {
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
                // pad or trim to 32 bytes
                byte[] tmp = new byte[32];
                System.arraycopy(keyBytes, 0, tmp, 0, Math.min(keyBytes.length, 32));
                keyBytes = tmp;
            }
            this.key = new SecretKeySpec(keyBytes, ALGO);
        }
    }

    private SecretKey generateKey(int bits) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGO);
            kg.init(bits);
            return kg.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);

            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            ByteBuffer bb = ByteBuffer.allocate(iv.length + cipherText.length);
            bb.put(iv);
            bb.put(cipherText);

            return Base64.getEncoder().encodeToString(bb.array());
        } catch (Exception e) {
            throw new RuntimeException("Erro na criptografia", e);
        }
    }

    public String decrypt(String cipherTextB64) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherTextB64);
            ByteBuffer bb = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_LENGTH];
            bb.get(iv);

            byte[] cipherText = new byte[bb.remaining()];
            bb.get(cipherText);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);

            byte[] plain = cipher.doFinal(cipherText);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Erro na decriptação", e);
        }
    }
}
