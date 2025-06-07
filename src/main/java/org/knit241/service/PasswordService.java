package org.knit241.service;

import org.knit241.clipboard.ClipboardService;
import org.knit241.crypto.EncryptionService;
import org.knit241.model.PasswordEntry;
import org.knit241.repository.PasswordRepository;
import org.knit241.security.MasterPasswordHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PasswordService {
    private final PasswordRepository repository;
    private final EncryptionService encryptionService;
    private final ClipboardService clipboardService;
    private final MasterPasswordHolder masterPasswordHolder;

    public PasswordService(PasswordRepository repository,
                           EncryptionService encryptionService,
                           ClipboardService clipboardService,
                           MasterPasswordHolder masterPasswordHolder) {
        this.repository = repository;
        this.encryptionService = encryptionService;
        this.clipboardService = clipboardService;
        this.masterPasswordHolder = masterPasswordHolder;
    }

    public void addPassword(String site, String login, String password) {
        if (!masterPasswordHolder.isPasswordSet()) {
            throw new IllegalStateException("Master password not set");
        }
        String normalizedSite = site.trim().toLowerCase();
        String encryptedPassword = encryptionService.encrypt(password);
        repository.add(new PasswordEntry(normalizedSite, login, encryptedPassword));
    }

    public void deletePassword(String site) {
        repository.delete(site);
    }

    public List<String> listSitesAndLogins() {
        return repository.findAll().stream()
                .map(entry -> entry.getSite() + " (" + entry.getLogin() + ")")
                .collect(Collectors.toList());
    }

    public void copyPassword(String site) {
        repository.findBySite(site).ifPresent(entry -> {
            String decryptedPassword = encryptionService.decrypt(entry.getEncryptedPassword());
            clipboardService.copyToClipboard(decryptedPassword);
        });
    }

    public List<PasswordEntry> getAllPasswords() {
        return repository.findAll().stream()
                .map(PasswordEntry::new) // копируем для защиты от изменений
                .collect(Collectors.toList());
    }

    public String getPassword(String site) {
        // Trim and normalize case for lookup
        String lookupKey = site.trim().toLowerCase();

        Optional<PasswordEntry> entry = repository.findAll().stream()
                .filter(e -> e.getSite().trim().equalsIgnoreCase(site))
                .findFirst();

        return entry.map(e -> encryptionService.decrypt(e.getEncryptedPassword()))
                .orElse(null);
    }

    public void saveToFile() {
        repository.save();
    }
}