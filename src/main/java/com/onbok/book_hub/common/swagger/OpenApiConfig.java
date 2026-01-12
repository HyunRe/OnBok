package com.onbok.book_hub.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String API_NAME = "OnBok API";
    private static final String API_VERSION = "1.0";
    private static final String API_DESCRIPTION = "OnBok API 명세서";

    /** 기본 문서 메타정보 */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(API_NAME)
                        .description(API_DESCRIPTION)
                        .version(API_VERSION));
    }

    /** 스캔 범위/경로 제어 (필요 시 패키지 변경) */
    @Bean
    public GroupedOpenApi totyPublicApi() {
        return GroupedOpenApi.builder()
                .group("toty-public")
                .packagesToScan("com.toty")   // RestController 들이 있는 루트 패키지
                .pathsToMatch("/**")          // 노출할 API 경로 패턴
                .build();
    }
}
