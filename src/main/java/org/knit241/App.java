package org.knit241;

import org.knit241.clipboard.ClipboardService;
import org.knit241.config.AppConfig;
import org.knit241.model.PasswordEntry;
import org.knit241.service.PasswordService;
import org.knit241.security.MasterPasswordHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.Console;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(AppConfig.class);
        var masterPasswordHolder = context.getBean(MasterPasswordHolder.class);
        var passwordService = context.getBean(PasswordService.class);

        char[] masterPassword = readPassword("Enter master password: ");

        if (masterPasswordHolder.isFirstRun()) {
            System.out.println("Setting up new master password...");
            masterPasswordHolder.setPassword(masterPassword);
        } else {
            if (!masterPasswordHolder.validateHash(masterPassword)) {
                System.err.println("Wrong master password!");
                System.exit(1);
            }
            masterPasswordHolder.setPassword(masterPassword);
        }
        var clipboardService = context.getBean(ClipboardService.class);

        logger.info("Password Manager started");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("1. Add password\n2. View passwords\n3. Copy password\n4. Exit");
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    System.out.print("Service: ");
                    String service = scanner.nextLine();
                    System.out.print("Username: ");
                    String username = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();
                    passwordService.addPassword(service, username, password);
                }
                case "2" -> {
                    List<PasswordEntry> entries = passwordService.getAllPasswords();
                    if (entries.isEmpty()) {
                        System.out.println("No passwords saved yet.");
                    } else {
                        System.out.println("Saved passwords:");
                        for (PasswordEntry entry : entries) {
                            System.out.println("- " + entry.getSite() +
                                    " (login: " + entry.getLogin() + ")");
                        }
                    }
                }
                case "3" -> {
                    System.out.print("Service to copy: ");
                    String service = scanner.nextLine();
                    try {
                        String password = passwordService.getPassword(service);
                        if (password != null) {
                            clipboardService.copyToClipboard(password);
                            System.out.println("Password for " + service +
                                    " copied to clipboard (will clear in 30 seconds)");
                        } else {
                            System.out.println("Service not found: " + service);
                        }
                    } catch (Exception e) {
                        System.err.println("Error copying password: " + e.getMessage());
                    }
                }
                case "4" -> {
                    passwordService.saveToFile();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    private static char[] readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            return console.readPassword(prompt);
        } else {
            System.out.print(prompt);
            return new Scanner(System.in).nextLine().toCharArray();
        }
    }
}
