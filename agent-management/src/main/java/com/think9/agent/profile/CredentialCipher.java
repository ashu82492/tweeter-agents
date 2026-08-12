package com.think9.agent.profile;

public interface CredentialCipher {
    EncryptedCredential encrypt(String password);

    String decrypt(EncryptedCredential credential);
}