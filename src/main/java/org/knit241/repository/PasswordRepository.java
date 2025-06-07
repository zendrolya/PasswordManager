package org.knit241.repository;
import org.knit241.model.PasswordEntry;

import java.util.List;
import java.util.Optional;

public interface PasswordRepository {
    void add(PasswordEntry entry);
    void delete(String site);
    Optional<PasswordEntry> findBySite(String site);
    List<PasswordEntry> findAll();

    void save();
    void load();
}