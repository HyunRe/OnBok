package com.onbok.book_hub.image.presentation;

import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.image.application.ImageService;
import com.onbok.book_hub.image.domain.model.Image;
import com.onbok.book_hub.image.dto.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class ImageApiController {
    private final ImageService imageService;

    /**
     * 로컬 파일 업로드
     */
    @PostMapping("/upload/local")
    public OnBokResponse<ImageUploadResponseDto> uploadLocal(@RequestParam("file") MultipartFile file) {
        try {
            Image image = imageService.uploadLocalFile(file);
            ImageUploadResponseDto responseDto = ImageUploadResponseDto.from(image);
            return OnBokResponse.success(responseDto);
        } catch (Exception e) {
            log.error("Local file upload failed: {}", e.getMessage());
            return OnBokResponse.error(HttpStatus.BAD_REQUEST, "파일 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * S3 URL 등록
     */
    @PostMapping("/upload/s3-url")
    public OnBokResponse<ImageUploadResponseDto> registerS3Url(@RequestParam("url") String s3Url,
                                                               @RequestParam(value = "filename", required = false) String originalFilename) {
        try {
            Image image = imageService.registerS3Url(s3Url, originalFilename);
            ImageUploadResponseDto responseDto = ImageUploadResponseDto.from(image);
            return OnBokResponse.success(responseDto);
        } catch (Exception e) {
            log.error("S3 URL registration failed: {}", e.getMessage());
            return OnBokResponse.error(HttpStatus.BAD_REQUEST,"S3 URL 등록에 실패했습니다: " + e.getMessage());
        }
    }

    /**
     * S3 파일 직접 업로드 (추후 구현)
     */
    @PostMapping("/upload/s3")
    public OnBokResponse<ImageUploadResponseDto> uploadToS3(@RequestParam("file") MultipartFile file) {
        try {
            Image image = imageService.uploadToS3(file);
            ImageUploadResponseDto responseDto = ImageUploadResponseDto.from(image);
            return OnBokResponse.success(responseDto);
        } catch (UnsupportedOperationException e) {
            return OnBokResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("S3 file upload failed: {}", e.getMessage());
            return OnBokResponse.error(HttpStatus.BAD_REQUEST, "S3 업로드에 실패했습니다: " + e.getMessage());
        }
    }
}
