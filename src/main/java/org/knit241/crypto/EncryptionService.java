package org.knit241.crypto;

public interface EncryptionService {
    String encrypt(String plainText);

    String decrypt(String cipherText);
}