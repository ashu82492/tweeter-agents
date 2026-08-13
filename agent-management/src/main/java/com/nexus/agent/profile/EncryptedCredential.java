package com.nexus.agent.profile;

public record EncryptedCredential(String ciphertext, String initializationVector) { }