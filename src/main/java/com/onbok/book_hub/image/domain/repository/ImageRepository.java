package com.onbok.book_hub.image.domain.repository;

import com.onbok.book_hub.image.domain.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
