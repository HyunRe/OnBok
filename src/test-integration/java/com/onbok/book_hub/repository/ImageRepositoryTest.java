package com.onbok.book_hub.repository;

import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.domain.model.ImageStorageType;
import com.onbok.book_hub.image.domain.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.onbok.book_hub")
@EnableJpaRepositories(basePackages = "com.onbok.book_hub")
@DisplayName("Image Repository 계층 테스트 - @DataJpaTest")
class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("이미지 저장 - 로컬 스토리지")
    void saveImage_localStorage() {
        // given
        Image image = Image.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-test-image.jpg")
                .filePath("/uploads/uuid-test-image.jpg")
                .url("http://localhost:8080/uploads/uuid-test-image.jpg")
                .contentType("image/jpeg")
                .fileSize(1024L)
                .storageType(ImageStorageType.LOCAL)
                .build();

        // when
        Image savedImage = imageRepository.save(image);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getOriginalFilename()).isEqualTo("test-image.jpg");
        assertThat(savedImage.getStoredFilename()).isEqualTo("uuid-test-image.jpg");
        assertThat(savedImage.getStorageType()).isEqualTo(ImageStorageType.LOCAL);
        assertThat(savedImage.getFileSize()).isEqualTo(1024L);
    }

    @Test
    @DisplayName("이미지 저장 - S3 스토리지")
    void saveImage_s3Storage() {
        // given
        Image image = Image.builder()
                .originalFilename("book-cover.jpg")
                .storedFilename("https://my-bucket.s3.amazonaws.com/books/cover-1.jpg")
                .filePath("https://my-bucket.s3.amazonaws.com/books/cover-1.jpg")
                .url("https://my-bucket.s3.amazonaws.com/books/cover-1.jpg")
                .contentType("image/*")
                .fileSize(0L)
                .storageType(ImageStorageType.S3)
                .build();

        // when
        Image savedImage = imageRepository.save(image);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedImage.getId()).isNotNull();
        assertThat(savedImage.getOriginalFilename()).isEqualTo("book-cover.jpg");
        assertThat(savedImage.getStorageType()).isEqualTo(ImageStorageType.S3);
        assertThat(savedImage.getUrl()).contains("s3.amazonaws.com");
    }

    @Test
    @DisplayName("이미지 ID로 조회 - findById")
    void findById_success() {
        // given
        Image image = createImage("test.jpg", ImageStorageType.LOCAL);

        entityManager.flush();
        entityManager.clear();

        // when
        Image foundImage = imageRepository.findById(image.getId()).orElse(null);

        // then
        assertThat(foundImage).isNotNull();
        assertThat(foundImage.getId()).isEqualTo(image.getId());
        assertThat(foundImage.getOriginalFilename()).isEqualTo("test.jpg");
    }

    @Test
    @DisplayName("이미지 URL로 조회 - findByUrl")
    void findByUrl_success() {
        // given
        String imageUrl = "http://localhost:8080/uploads/test-image.jpg";
        Image image = Image.builder()
                .originalFilename("test-image.jpg")
                .storedFilename("uuid-test-image.jpg")
                .filePath("/uploads/uuid-test-image.jpg")
                .url(imageUrl)
                .contentType("image/jpeg")
                .fileSize(2048L)
                .storageType(ImageStorageType.LOCAL)
                .build();
        imageRepository.save(image);

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<Image> foundImage = imageRepository.findByUrl(imageUrl);

        // then
        assertThat(foundImage).isPresent();
        assertThat(foundImage.get().getUrl()).isEqualTo(imageUrl);
    }

    @Test
    @DisplayName("스토리지 타입별 조회 - findByStorageType")
    void findByStorageType_success() {
        // given
        Image localImage1 = createImage("local1.jpg", ImageStorageType.LOCAL);
        Image localImage2 = createImage("local2.jpg", ImageStorageType.LOCAL);
        Image s3Image = createImage("s3-image.jpg", ImageStorageType.S3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> localImages = imageRepository.findByStorageType(ImageStorageType.LOCAL);
        List<Image> s3Images = imageRepository.findByStorageType(ImageStorageType.S3);

        // then
        assertThat(localImages).hasSize(2);
        assertThat(s3Images).hasSize(1);
    }

    @Test
    @DisplayName("컨텐츠 타입별 조회 - findByContentType")
    void findByContentType_success() {
        // given
        Image jpegImage1 = createImageWithContentType("image1.jpg", "image/jpeg", ImageStorageType.LOCAL);
        Image jpegImage2 = createImageWithContentType("image2.jpg", "image/jpeg", ImageStorageType.LOCAL);
        Image pngImage = createImageWithContentType("image.png", "image/png", ImageStorageType.LOCAL);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> jpegImages = imageRepository.findByContentType("image/jpeg");
        List<Image> pngImages = imageRepository.findByContentType("image/png");

        // then
        assertThat(jpegImages).hasSize(2);
        assertThat(pngImages).hasSize(1);
    }

    @Test
    @DisplayName("이미지 삭제 - deleteById")
    void deleteImage_success() {
        // given
        Image image = createImage("delete-test.jpg", ImageStorageType.LOCAL);
        Long imageId = image.getId();

        entityManager.flush();
        entityManager.clear();

        // when
        imageRepository.deleteById(imageId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Image> deletedImage = imageRepository.findById(imageId);
        assertThat(deletedImage).isEmpty();
    }

    @Test
    @DisplayName("전체 이미지 조회 - findAll")
    void findAll_success() {
        // given
        createImage("image1.jpg", ImageStorageType.LOCAL);
        createImage("image2.jpg", ImageStorageType.LOCAL);
        createImage("image3.jpg", ImageStorageType.S3);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> allImages = imageRepository.findAll();

        // then
        assertThat(allImages).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("이미지 개수 조회 - count")
    void countImages_success() {
        // given
        createImage("image1.jpg", ImageStorageType.LOCAL);
        createImage("image2.jpg", ImageStorageType.LOCAL);

        entityManager.flush();
        entityManager.clear();

        // when
        long count = imageRepository.count();

        // then
        assertThat(count).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("파일 크기별 조회 - findByFileSizeGreaterThan")
    void findByFileSizeGreaterThan_success() {
        // given
        Image smallImage = createImageWithSize("small.jpg", 500L, ImageStorageType.LOCAL);
        Image mediumImage = createImageWithSize("medium.jpg", 5000L, ImageStorageType.LOCAL);
        Image largeImage = createImageWithSize("large.jpg", 50000L, ImageStorageType.LOCAL);

        entityManager.flush();
        entityManager.clear();

        // when
        List<Image> largeImages = imageRepository.findByFileSizeGreaterThan(4000L);

        // then
        assertThat(largeImages).hasSize(2); // mediumImage, largeImage
        assertThat(largeImages).extracting("fileSize").containsExactlyInAnyOrder(5000L, 50000L);
    }

    @Test
    @DisplayName("로컬 이미지와 S3 이미지 구분 저장")
    void saveImagesWithDifferentStorageTypes() {
        // given
        Image localImage = createImage("local-image.jpg", ImageStorageType.LOCAL);
        Image s3Image = createImage("s3-image.jpg", ImageStorageType.S3);

        entityManager.flush();
        entityManager.clear();

        // when
        Image foundLocalImage = imageRepository.findById(localImage.getId()).orElseThrow();
        Image foundS3Image = imageRepository.findById(s3Image.getId()).orElseThrow();

        // then
        assertThat(foundLocalImage.getStorageType()).isEqualTo(ImageStorageType.LOCAL);
        assertThat(foundS3Image.getStorageType()).isEqualTo(ImageStorageType.S3);
    }

    // === Helper Methods ===

    private Image createImage(String filename, ImageStorageType storageType) {
        Image image = Image.builder()
                .originalFilename(filename)
                .storedFilename("uuid-" + filename)
                .filePath("/uploads/uuid-" + filename)
                .url("http://localhost:8080/uploads/uuid-" + filename)
                .contentType("image/jpeg")
                .fileSize(1024L)
                .storageType(storageType)
                .build();
        return imageRepository.save(image);
    }

    private Image createImageWithContentType(String filename, String contentType, ImageStorageType storageType) {
        Image image = Image.builder()
                .originalFilename(filename)
                .storedFilename("uuid-" + filename)
                .filePath("/uploads/uuid-" + filename)
                .url("http://localhost:8080/uploads/uuid-" + filename)
                .contentType(contentType)
                .fileSize(1024L)
                .storageType(storageType)
                .build();
        return imageRepository.save(image);
    }

    private Image createImageWithSize(String filename, Long fileSize, ImageStorageType storageType) {
        Image image = Image.builder()
                .originalFilename(filename)
                .storedFilename("uuid-" + filename)
                .filePath("/uploads/uuid-" + filename)
                .url("http://localhost:8080/uploads/uuid-" + filename)
                .contentType("image/jpeg")
                .fileSize(fileSize)
                .storageType(storageType)
                .build();
        return imageRepository.save(image);
    }
}
