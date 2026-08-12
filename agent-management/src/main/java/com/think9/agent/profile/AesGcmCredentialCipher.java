package com.think9.agent.profile;

import com.think9.agent.config.AgentManagementProperties;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AesGcmCredentialCipher implements CredentialCipher {
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom;

    @Autowired
    public AesGcmCredentialCipher(AgentManagementProperties properties) {
        this(decodeKey(properties.getCredentialEncryptionKey()), new SecureRandom());
    }

    AesGcmCredentialCipher(SecretKey secretKey, SecureRandom secureRandom) {
        this.secretKey = secretKey;
        this.secureRandom = secureRandom;
    }

    @Override
    public EncryptedCredential encrypt(String password) {
        try {
            byte[] initializationVector = new byte[IV_LENGTH];
            secureRandom.nextBytes(initializationVector);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new javax.crypto.spec.GCMParameterSpec(128, initializationVector));
            return new EncryptedCredential(Base64.getEncoder().encodeToString(cipher.doFinal(password.getBytes(StandardCharsets.UTF_8))),
                    Base64.getEncoder().encodeToString(initializationVector));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("unable to encrypt agent credential", exception);
        }
    }

    @Override
    public String decrypt(EncryptedCredential credential) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new javax.crypto.spec.GCMParameterSpec(128,
                    Base64.getDecoder().decode(credential.initializationVector())));
            return new String(cipher.doFinal(Base64.getDecoder().decode(credential.ciphertext())), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("unable to decrypt agent credential", exception);
        }
    }

    private static SecretKey decodeKey(String encodedKey) {
        byte[] key = Base64.getDecoder().decode(encodedKey);
        if (key.length != 16 && key.length != 24 && key.length != 32) {
            throw new IllegalArgumentException("agent credential encryption key must decode to 16, 24, or 32 bytes");
        }
        return new SecretKeySpec(key, "AES");
    }
}