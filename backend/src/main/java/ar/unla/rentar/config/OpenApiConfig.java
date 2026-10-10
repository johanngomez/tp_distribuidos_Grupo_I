package ar.unla.rentar.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la documentación OpenAPI/Swagger.
 *
 * <p>El esquema declarado permite que Swagger UI envíe el JWT en las
 * operaciones protegidas mediante el botón "Authorize".</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rentarOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rentar API")
                        .description("API REST del sistema de alquiler de vehículos")
                        .version("Hito 1"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
