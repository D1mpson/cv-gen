package com.example.cvgenerator.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        if (jdbcTemplate != null) {
            try {
                jdbcTemplate.execute("SELECT 1");
                logger.info("Successfully connected to database");
            } catch (Exception e) {
                logger.error("Failed to connect to database: {}", e.getMessage());
            }
        }
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        String host = env.getProperty("MYSQLHOST", "localhost");
        String port = env.getProperty("MYSQLPORT", "3306");
        String database = env.getProperty("MYSQLDATABASE", "railway");
        String username = env.getProperty("MYSQLUSER", "root");
        String password = env.getProperty("MYSQLPASSWORD", "");

        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true&autoReconnect=true",
                host, port, database);

        logger.info("Database URL: {}", url);
        logger.info("Database User: {}", username);
        logger.info("Database is configured with host: {}, port: {}, database: {}", host, port, database);

        HikariDataSource dataSource = (HikariDataSource) DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .type(HikariDataSource.class)
                .build();

        // Важливі налаштування
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);

        return dataSource;
    }
}