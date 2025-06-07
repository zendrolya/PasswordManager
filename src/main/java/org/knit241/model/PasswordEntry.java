package org.knit241.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PasswordEntry {
    private final String site;
    private final String login;
    private final String encryptedPassword;

    @JsonCreator
    public PasswordEntry(
            @JsonProperty("site") String site,
            @JsonProperty("login") String login,
            @JsonProperty("encryptedPassword") String encryptedPassword) {
        this.site = site;
        this.login = login;
        this.encryptedPassword = encryptedPassword;
    }

    public PasswordEntry(PasswordEntry other) {
        this.site = other.site;
        this.login = other.login;
        this.encryptedPassword = other.encryptedPassword;
    }

    public String getSite() { return site; }
    public String getLogin() { return login; }
    public String getEncryptedPassword() { return encryptedPassword; }
    @Override
    public String toString() {
        return "Site: " + site + ", Login: " + login;
    }
}