package com.career.platform.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    public static final String API_KEY_AUTH = "apiKeyAuth";

    @Bean
    public OpenAPI platformOpenApi() {
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Access Token returned by /api/auth/login or /api/auth/refresh");
        SecurityScheme apiKeyAuth = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-API-Key")
                .description("API Key shown once when it is created");

        return new OpenAPI()
                .info(new Info()
                        .title("职业能力大数据服务平台 API")
                        .description("认证、权限管理和第三方开放接口")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, bearerAuth)
                        .addSecuritySchemes(API_KEY_AUTH, apiKeyAuth));
    }

    @Bean
    public OpenApiCustomiser openApiSecurityCustomiser() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            if (path.startsWith("/api/open/v1/")) {
                SecurityRequirement doubleAuthentication = new SecurityRequirement()
                        .addList(BEARER_AUTH)
                        .addList(API_KEY_AUTH);
                pathItem.readOperations().forEach(operation ->
                        operation.setSecurity(List.of(doubleAuthentication)));
            }
        });
    }
}
