package com.onbok.book_hub.image.domain.repository;

import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.domain.model.ImageStorageType;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    // 이미지 URL로 조회
    Optional<Image> findByUrl(String url);

    // 스토리지 타입별 조회
    List<Image> findByStorageType(ImageStorageType storageType);

    // 컨텐츠 타입별 조회
    List<Image> findByContentType(String contentType);

    // 파일 크기별 조회
    List<Image> findByFileSizeGreaterThan(Long size);
}
