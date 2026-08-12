package com.think9.agent.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.security.SecureRandom;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

class AesGcmCredentialCipherTest {
    @Test
    void encryptsAndDecryptsIndividualAgentCredentials() {
        AesGcmCredentialCipher cipher = new AesGcmCredentialCipher(new SecretKeySpec(new byte[32], "AES"), new SecureRandom());

        EncryptedCredential encrypted = cipher.encrypt("agent-password");

        assertNotEquals("agent-password", encrypted.ciphertext());
        assertEquals("agent-password", cipher.decrypt(encrypted));
    }
}