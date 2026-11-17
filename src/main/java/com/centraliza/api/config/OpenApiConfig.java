package com.centraliza.api.config;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI centralizaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Centraliza API")
                        .description("""
                                API centralizadora de serviços externos.
                                Atualmente realiza consultas de endereços
                                utilizando a API pública ViaCEP.
                                """)
                        .version("1.0.0"));
    }
}

