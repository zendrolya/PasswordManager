package org.knit241.security;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class MasterPasswordHolder {
    private char[] masterPassword;
    private boolean initialized = false;
    private static final String HASH_FILE = "masterpass.hash";

    public boolean isPasswordSet() {
        return masterPassword != null;
    }

    public boolean checkPassword(char[] inputPassword) {
        if (masterPassword == null) return false;
        return Arrays.equals(masterPassword, inputPassword);
    }

    public void setPassword(char[] masterPassword) {
        this.masterPassword = Arrays.copyOf(masterPassword, masterPassword.length);
        this.initialized = true;
        saveHash();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public char[] getPassword() {
        if (masterPassword == null) {
            return null;
        }
        return Arrays.copyOf(masterPassword, masterPassword.length); // Возвращаем копию
    }


    public boolean isFirstRun() {
        return !new File(HASH_FILE).exists();
    }

    private void saveHash() {
        try {
            byte[] hash = calculateHash(masterPassword);
            Files.write(new File(HASH_FILE).toPath(), hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save master password hash", e);
        }
    }

    public boolean validateHash(char[] inputPassword) {
        try {
            if (!new File(HASH_FILE).exists()) return true;

            byte[] savedHash = Files.readAllBytes(new File(HASH_FILE).toPath());
            byte[] inputHash = calculateHash(inputPassword);
            return MessageDigest.isEqual(savedHash, inputHash);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] calculateHash(char[] password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] bytes = new String(password).getBytes();
        return md.digest(bytes);
    }

    public void clear() {
        if (masterPassword != null) {
            Arrays.fill(masterPassword, '\0');
        }
    }
}