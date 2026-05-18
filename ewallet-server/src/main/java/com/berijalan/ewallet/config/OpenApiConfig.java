package com.berijalan.ewallet.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    /**
     * Mendefinisikan metadata API dan skema JWT yang dipakai endpoint protected.
     *
     * @return konfigurasi OpenAPI untuk springdoc.
     */
    @Bean
    public OpenAPI astraPayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AstraPay E-Wallet Topup API")
                        .version("v1")
                        .description("Dokumentasi API untuk autentikasi, wallet, transaksi, merchant, dan pengaturan admin."))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
