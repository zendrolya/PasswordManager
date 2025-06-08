package org.knit241.crypto;

import org.knit241.security.MasterPasswordHolder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class AesEncryptionService implements EncryptionService {
    private static final String SECRET_ALGO = "PBKDF2WithHmacSHA256";
    private static final String ENCRYPTION_ALGO = "AES/CBC/PKCS5Padding";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 16;
    private static final int SALT_LENGTH = 16;

    private final MasterPasswordHolder masterPasswordHolder;
    private final SecureRandom secureRandom = new SecureRandom();
    private byte[] salt;

    public AesEncryptionService(MasterPasswordHolder masterPasswordHolder) {
        this.masterPasswordHolder = masterPasswordHolder;
        this.salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(this.salt);
    }

    private SecretKeySpec getKey() throws Exception {
        char[] passwordChars = masterPasswordHolder.getPassword();
        if (passwordChars == null) {
            throw new IllegalStateException("Master password not set");
        }

        KeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_ALGO);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    @Override
    public String encrypt(String plainText) {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, getKey(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());

            byte[] combined = new byte[SALT_LENGTH + IV_LENGTH + encrypted.length];
            System.arraycopy(salt, 0, combined, 0, SALT_LENGTH);
            System.arraycopy(iv, 0, combined, SALT_LENGTH, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, SALT_LENGTH + IV_LENGTH, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String decrypt(String cipherText) {
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            if (combined.length < SALT_LENGTH + IV_LENGTH) {
                throw new IllegalArgumentException("Invalid cipher text");
            }

            // Извлекаем salt, iv и зашифрованные данные
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - SALT_LENGTH - IV_LENGTH];

            System.arraycopy(combined, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combined, SALT_LENGTH, iv, 0, IV_LENGTH);
            System.arraycopy(combined, SALT_LENGTH + IV_LENGTH, encrypted, 0, encrypted.length);

            byte[] originalSalt = this.salt;
            this.salt = salt;

            try {
                Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO);
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, getKey(), ivSpec);
                byte[] decrypted = cipher.doFinal(encrypted);
                return new String(decrypted);
            } finally {
                this.salt = originalSalt;
            }
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}