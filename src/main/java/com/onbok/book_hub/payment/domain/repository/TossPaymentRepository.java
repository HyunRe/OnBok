package com.onbok.book_hub.payment.domain.repository;

import com.onbok.book_hub.payment.domain.model.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TossPaymentRepository extends JpaRepository<TossPayment, Long> {
}
