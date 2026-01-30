package com.onbok.book_hub.image;

import com.onbok.book_hub.image.application.ImageService;
import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.domain.model.ImageStorageType;
import com.onbok.book_hub.image.domain.repository.ImageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Image 로컬 업로드 통합 테스트")
class ImageLocalUploadTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @AfterEach
    void cleanUp() throws IOException {
        // 테스트 후 업로드된 파일 삭제
        File uploadDir = new File(uploadPath);
        if (uploadDir.exists() && uploadDir.isDirectory()) {
            File[] files = uploadDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains("test-image")) {
                        file.delete();
                    }
                }
            }
        }
    }

    @Test
    @DisplayName("로컬 파일 업로드 - 정상 케이스")
    void uploadLocalFile_success() throws IOException {
        // given
        String originalFilename = "test-image.jpg";
        byte[] content = "test image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                originalFilename,
                "image/jpeg",
                content
        );

        // when
        Image image = imageService.uploadLocalFile(file);

        // then
        assertThat(image).isNotNull();
        assertThat(image.getId()).isNotNull();
        assertThat(image.getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(image.getStoredFilename()).isNotEmpty();
        assertThat(image.getStoredFilename()).contains(".jpg");
        assertThat(image.getStorageType()).isEqualTo(ImageStorageType.LOCAL);
        assertThat(image.getFileSize()).isEqualTo(content.length);
        assertThat(image.getContentType()).isEqualTo("image/jpeg");
        assertThat(image.getUrl()).contains(image.getStoredFilename());

        // 실제 파일이 저장되었는지 확인
        Path filePath = Paths.get(uploadPath, image.getStoredFilename());
        assertThat(Files.exists(filePath)).isTrue();
    }

    @Test
    @DisplayName("로컬 파일 업로드 - 저장 경로 반환")
    void uploadLocalFile_returnsCorrectPath() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.png",
                "image/png",
                "image data".getBytes()
        );

        // when
        Image image = imageService.uploadLocalFile(file);

        // then
        assertThat(image.getFilePath()).contains(uploadPath);
        assertThat(image.getFilePath()).endsWith(".png");
    }

    @Test
    @DisplayName("로컬 파일 업로드 - 다양한 확장자 지원")
    void uploadLocalFile_supportsVariousExtensions() {
        // given & when & then
        String[] extensions = {".jpg", ".png", ".gif", ".bmp"};

        for (String ext : extensions) {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "test-image" + ext,
                    "image/" + ext.substring(1),
                    "image data".getBytes()
            );

            Image image = imageService.uploadLocalFile(file);

            assertThat(image.getStoredFilename()).endsWith(ext);
            assertThat(image.getOriginalFilename()).endsWith(ext);
        }
    }

    @Test
    @DisplayName("로컬 파일 업로드 - UUID로 고유한 파일명 생성")
    void uploadLocalFile_generatesUniqueFilename() {
        // given
        MockMultipartFile file1 = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content2".getBytes()
        );

        // when
        Image image1 = imageService.uploadLocalFile(file1);
        Image image2 = imageService.uploadLocalFile(file2);

        // then
        assertThat(image1.getStoredFilename()).isNotEqualTo(image2.getStoredFilename());
        assertThat(image1.getOriginalFilename()).isEqualTo(image2.getOriginalFilename());
    }

    @Test
    @DisplayName("로컬 파일 업로드 - 대용량 파일")
    void uploadLocalFile_largeFile() {
        // given
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                largeContent
        );

        // when
        Image image = imageService.uploadLocalFile(file);

        // then
        assertThat(image.getFileSize()).isEqualTo(largeContent.length);
    }
}
