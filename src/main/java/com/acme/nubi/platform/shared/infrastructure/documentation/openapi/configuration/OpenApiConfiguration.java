package com.acme.nubi.platform.shared.infrastructure.documentation.openapi.configuration;

import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI nubiPlatformOpenApi() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes("bearer",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"))
                .info(new Info()
                        .title("Nubi Platform API")
                        .description("Nubi RESTful API: support for autistic children and adolescents and their parents during a crisis.")
                        .version("v1.0.0"));
    }
}
