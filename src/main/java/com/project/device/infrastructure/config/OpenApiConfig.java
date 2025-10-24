package com.project.device.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for OpenAPI/Swagger documentation.
 *
 * <p>Configures the API documentation accessible via Swagger UI.
 */
@Configuration
public class OpenApiConfig {

  /**
   * Configures OpenAPI documentation.
   *
   * @return configured OpenAPI instance
   */
  @Bean
  public OpenAPI deviceDomainOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Device Management API")
                .description(
                    "REST API for managing devices with hexagonal architecture. "
                        + "Provides endpoints for creating, reading, updating, and deleting devices, "
                        + "with support for filtering by brand and state.")
                .version("1.0.0")
                .contact(
                    new Contact().name("Device Management Team").email("support@devicedomain.com"))
                .license(
                    new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
  }
}
