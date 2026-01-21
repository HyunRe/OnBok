package com.onbok.book_hub.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 상대 경로를 절대 경로로 변환
        File uploadDir = new File(uploadPath);
        String absolutePath = uploadDir.getAbsolutePath();

        // 절대 경로가 /로 끝나지 않으면 추가
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath += File.separator;
        }

        log.info("Static resource mapping: /uploads/** -> file:{}", absolutePath);

        // 업로드된 파일을 /uploads/** URL로 접근 가능하도록 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
