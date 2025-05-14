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

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@Profile("prod")
public class DatabaseConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);

    @Autowired
    private Environment env;

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

        // Критичні налаштування для отримання ID
        dataSource.setAutoCommit(true);
        dataSource.setMaximumPoolSize(5);
        dataSource.setMinimumIdle(2);

        // Додаткові налаштування для покращення продуктивності
        Properties props = new Properties();
        props.setProperty("useConfigs", "maxPerformance");
        props.setProperty("rewriteBatchedStatements", "true");
        props.setProperty("cachePrepStmts", "true");
        props.setProperty("prepStmtCacheSize", "250");
        props.setProperty("prepStmtCacheSqlLimit", "2048");
        props.setProperty("useServerPrepStmts", "true");
        props.setProperty("useLocalSessionState", "true");
        props.setProperty("cacheResultSetMetadata", "true");
        props.setProperty("cacheServerConfiguration", "true");
        props.setProperty("elideSetAutoCommits", "true");
        props.setProperty("maintainTimeStats", "false");
        props.setProperty("useSSL", "false");
        dataSource.setDataSourceProperties(props);

        return dataSource;
    }
}