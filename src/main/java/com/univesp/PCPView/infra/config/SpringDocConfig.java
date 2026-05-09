package com.univesp.PCPView.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API PCPView")
                        .version("v1")
                        .description("API REST que gerencia o PCP da empresa EquipSea.")
                        .contact(new Contact()
                                .name("LinkedIn")
                                .url("https://www.linkedin.com/in/carlos-daniel-martins-sanguino-72b46a2a0/")
                        )
                );
    }
}
