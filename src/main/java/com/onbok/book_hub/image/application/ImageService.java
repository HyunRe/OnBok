package com.onbok.book_hub.image.application;

import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.domain.model.ImageStorageType;
import com.onbok.book_hub.image.domain.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ImageService {
    private final ImageRepository imageRepository;

    @Value("${file.upload.path:/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:http://localhost:8080/uploads}")
    private String urlPrefix;

    /**
     * 로컬 파일 시스템에 이미지 저장
     */
    public Image uploadLocalFile(MultipartFile file) {
        try {
            // 업로드 디렉토리 생성
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 파일명 생성 (UUID + 원본 확장자)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String storedFilename = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = Paths.get(uploadPath, storedFilename);
            Files.copy(file.getInputStream(), filePath);

            // URL 생성
            String url = urlPrefix + "/" + storedFilename;

            // Image 엔티티 생성 및 저장
            Image image = Image.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .storageType(ImageStorageType.LOCAL)
                    .url(url)
                    .build();

            return imageRepository.save(image);

        } catch (IOException e) {
            log.error("Failed to upload local file: {}", e.getMessage());
            throw new RuntimeException("파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * S3 URL을 사용한 이미지 등록
     */
    public Image registerS3Url(String s3Url, String originalFilename) {
        Image image = Image.builder()
                .originalFilename(originalFilename != null ? originalFilename : "s3-image")
                .storedFilename(s3Url)
                .filePath(s3Url)
                .fileSize(0L)
                .contentType("image/*")
                .storageType(ImageStorageType.S3)
                .url(s3Url)
                .build();

        return imageRepository.save(image);
    }

    /**
     * S3에 파일 업로드 (실제 AWS SDK 사용)
     * TODO: AWS SDK 의존성 추가 후 구현
     */
    public Image uploadToS3(MultipartFile file) {
        // AWS S3 업로드 로직은 추후 구현
        throw new UnsupportedOperationException("S3 파일 업로드는 아직 구현되지 않았습니다. S3 URL을 직접 입력해주세요.");
    }
}
