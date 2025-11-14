package com.aspacelife.postbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
@Slf4j
public class DatabaseConfig {

    @PostConstruct
    public void init() {
        log.info("Initializing SQLite database configuration");
        log.info("Database file: posts.db");
    }
}
