package com.p2p;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@EnableR2dbcRepositories
@EnableTransactionManagement
@OpenAPIDefinition(
    info = @Info(
        title = "P2P代購平台 API",
        version = "1.0",
        description = "P2P代購平台的RESTful API文檔"
    )
)
public class P2PApplication {
    public static void main(String[] args) {
        SpringApplication.run(P2PApplication.class, args);
    }
} 