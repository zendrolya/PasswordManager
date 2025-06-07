package org.knit241.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;
import org.knit241.model.PasswordEntry;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class InMemoryPasswordRepository implements PasswordRepository {
    private final Map<String, PasswordEntry> storage = new HashMap<>();
    private static final String FILE_PATH = "passwords.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InMemoryPasswordRepository() {
        load();
    }

    @Override
    public void add(PasswordEntry entry) {
        storage.put(entry.getSite(), entry);
    }

    @Override
    public void delete(String site) {
        storage.remove(site);
    }

    @Override
    public Optional<PasswordEntry> findBySite(String site) {
        return Optional.ofNullable(storage.get(site));
    }

    @Override
    public List<PasswordEntry> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void save() {
        try {
            objectMapper.writeValue(new File(FILE_PATH), storage.values());
        } catch (IOException e) {
            throw new RuntimeException("Error saving passwords to file", e);
        }
    }

    @Override
    public void load() {
        File file = new File(FILE_PATH);
        if (file.exists() && file.length() > 0) {
            try {
                List<PasswordEntry> entries = objectMapper.readValue(file, new TypeReference<List<PasswordEntry>>() {});
                storage.clear();
                for (PasswordEntry entry : entries) {
                    storage.put(entry.getSite(), entry);
                }
            } catch (IOException e) {
                System.err.println("Error loading passwords from file: " + e.getMessage());
            }
        }
    }
}