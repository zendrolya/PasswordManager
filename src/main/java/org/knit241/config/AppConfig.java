package org.knit241.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "org.knit241.service",
        "org.knit241.repository",
        "org.knit241.crypto",
        "org.knit241.security",
        "org.knit241.clipboard",
        "org.knit241.model"
})
public class AppConfig {
}