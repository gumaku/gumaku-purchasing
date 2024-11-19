package com.p2p.config;

import io.r2dbc.pool.PoolingConnectionFactoryProvider;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String url;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Value("${spring.r2dbc.pool.initial-size:10}")
    private int initialSize;

    @Value("${spring.r2dbc.pool.max-size:30}")
    private int maxSize;

    @Value("${spring.r2dbc.pool.max-idle-time:30}")
    private Duration maxIdleTime;

    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        PostgresqlConnectionConfiguration pgConfig = PostgresqlConnectionConfiguration.builder()
            .host(extractHost(url))
            .port(extractPort(url))
            .database(extractDatabase(url))
            .username(username)
            .password(password)
            .build();

        ConnectionPoolConfiguration poolConfig = ConnectionPoolConfiguration.builder()
            .connectionFactory(new PostgresqlConnectionFactory(pgConfig))
            .name("pg-connection-pool")
            .initialSize(initialSize)
            .maxSize(maxSize)
            .maxIdleTime(maxIdleTime)
            .validationQuery("SELECT 1")
            .build();

        return new ConnectionPool(poolConfig);
    }

    private String extractHost(String url) {
        // r2dbc:postgresql://localhost:5432/dbname
        String[] parts = url.split("://")[1].split(":");
        return parts[0];
    }

    private int extractPort(String url) {
        String[] parts = url.split("://")[1].split(":");
        return Integer.parseInt(parts[1].split("/")[0]);
    }

    private String extractDatabase(String url) {
        String[] parts = url.split("://")[1].split("/");
        return parts[1];
    }
} 