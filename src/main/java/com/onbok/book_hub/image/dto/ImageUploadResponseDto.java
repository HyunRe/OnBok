package com.onbok.book_hub.image.dto;

import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.domain.model.ImageStorageType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageUploadResponseDto {
    private Long id;
    private String originalFilename;
    private String url;
    private ImageStorageType storageType;
    private Long fileSize;

    public static ImageUploadResponseDto from(Image image) {
        return ImageUploadResponseDto.builder()
                .id(image.getId())
                .originalFilename(image.getOriginalFilename())
                .url(image.getUrl())
                .storageType(image.getStorageType())
                .fileSize(image.getFileSize())
                .build();
    }
}
