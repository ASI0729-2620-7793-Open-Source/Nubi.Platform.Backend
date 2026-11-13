package com.acme.nubi.platform.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI nubiPlatformOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nubi Platform API")
                        .description("RESTful API de Nubi: apoyo a niños y adolescentes autistas y a sus padres durante una crisis.")
                        .version("v1.0.0"));
    }
}
