package com.futurenbetter.saas;

import com.futurenbetter.saas.config.DotEnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaasApplication {
    public static void main(String[] args) {

        DotEnvConfig.loadEnv();
        SpringApplication.run(SaasApplication.class, args);
    }
}
