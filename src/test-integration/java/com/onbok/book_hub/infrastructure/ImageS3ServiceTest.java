package com.onbok.book_hub.infrastructure;

import com.onbok.book_hub.image.application.ImageService;
import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.domain.model.ImageStorageType;
import com.onbok.book_hub.image.domain.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("Image S3 업로드 통합 테스트")
class ImageS3ServiceTest {

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageRepository imageRepository;

    @Test
    @DisplayName("S3 URL 등록 - 정상 케이스")
    void registerS3Url_success() {
        // given
        String s3Url = "https://my-bucket.s3.ap-northeast-2.amazonaws.com/books/cover-1.jpg";
        String originalFilename = "book-cover.jpg";

        // when
        Image image = imageService.registerS3Url(s3Url, originalFilename);

        // then
        assertThat(image).isNotNull();
        assertThat(image.getId()).isNotNull();
        assertThat(image.getOriginalFilename()).isEqualTo(originalFilename);
        assertThat(image.getStoredFilename()).isEqualTo(s3Url);
        assertThat(image.getFilePath()).isEqualTo(s3Url);
        assertThat(image.getUrl()).isEqualTo(s3Url);
        assertThat(image.getStorageType()).isEqualTo(ImageStorageType.S3);
    }

    @Test
    @DisplayName("S3 URL 등록 - 파일명 없이 등록")
    void registerS3Url_withoutFilename() {
        // given
        String s3Url = "https://my-bucket.s3.amazonaws.com/images/image-123.png";

        // when
        Image image = imageService.registerS3Url(s3Url, null);

        // then
        assertThat(image.getOriginalFilename()).isEqualTo("s3-image");
        assertThat(image.getUrl()).isEqualTo(s3Url);
    }

    @Test
    @DisplayName("S3 URL 등록 - 파일 메타데이터 처리")
    void registerS3Url_metadataProcessing() {
        // given
        String s3Url = "https://example.s3.amazonaws.com/path/to/file.jpg";
        String originalFilename = "original-image.jpg";

        // when
        Image image = imageService.registerS3Url(s3Url, originalFilename);

        // then
        assertThat(image.getFileSize()).isEqualTo(0L); // S3 URL 등록 시 크기는 0
        assertThat(image.getContentType()).isEqualTo("image/*");
        assertThat(image.getStorageType()).isEqualTo(ImageStorageType.S3);
    }

    @Test
    @DisplayName("S3 직접 업로드 - 아직 구현되지 않음 (UnsupportedOperationException)")
    void uploadToS3_notImplemented() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> imageService.uploadToS3(file))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("S3 파일 업로드는 아직 구현되지 않았습니다");
    }

    @Test
    @DisplayName("여러 S3 URL 등록 - 배치 처리")
    void registerMultipleS3Urls() {
        // given
        String[] s3Urls = {
                "https://bucket.s3.amazonaws.com/image1.jpg",
                "https://bucket.s3.amazonaws.com/image2.png",
                "https://bucket.s3.amazonaws.com/image3.gif"
        };

        // when
        for (int i = 0; i < s3Urls.length; i++) {
            Image image = imageService.registerS3Url(s3Urls[i], "image" + (i + 1));

            // then
            assertThat(image.getUrl()).isEqualTo(s3Urls[i]);
            assertThat(image.getStorageType()).isEqualTo(ImageStorageType.S3);
        }

        // 전체 이미지 개수 확인
        long count = imageRepository.count();
        assertThat(count).isGreaterThanOrEqualTo(s3Urls.length);
    }
}
