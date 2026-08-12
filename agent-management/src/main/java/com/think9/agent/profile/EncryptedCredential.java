package com.think9.agent.profile;

public record EncryptedCredential(String ciphertext, String initializationVector) { }