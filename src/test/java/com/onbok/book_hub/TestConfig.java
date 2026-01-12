package com.onbok.book_hub;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@TestConfiguration
@EnableAutoConfiguration(exclude = {
        ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRestClientAutoConfiguration.class
})
@ComponentScan(
        basePackages = "com.lion.demo",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.onbok\\.book_hub\\.book\\.application\\.service\\.BookEsService"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.onbok\\.book_hub\\.book\\.presentation\\..*BookEs.*"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.onbok\\.book_hub\\.book\\.domain\\.BookEsRepository")
        }
)
public class TestConfig {
}
