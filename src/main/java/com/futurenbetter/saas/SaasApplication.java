package com.futurenbetter.saas;

import com.futurenbetter.saas.config.DotEnvConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class SaasApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DotEnvConfig.loadEnv();
        SpringApplication.run(SaasApplication.class, args);
    }
}
