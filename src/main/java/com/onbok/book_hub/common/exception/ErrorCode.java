package com.onbok.book_hub.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "잘못된 비밀번호 형식입니다."),

    // 책
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 책입니다."),

    // 책(일레스틱 서치)
    BOOK_ES_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 책(일레스틱 서치)입니다."),

    // 장바구니
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 장바구니입니다."),

    // 주소
    DELIVERY_ADRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주소입니다."),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 주문입니다."),

    // 결제
    TOSS_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 결제입니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "결제 승인에 실패하였습니다."),
    PAYMENT_CONFIRM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 승인 중 예기치 못한 오류가 발생했습니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_REQUEST, "결제 취소에 실패하였습니다."),
    PAYMENT_CANCEL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "결제 취소 중 예기치 못한 오류가 발생했습니다."),
    REFUND_FAILED(HttpStatus.BAD_REQUEST, "환불 처리에 실패하였습니다."),
    REFUND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "환불 처리 중 예기치 못한 오류가 발생했습니다."),

    // 리뷰
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 리뷰입니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 도서에 리뷰를 작성하셨습니다."),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INVALID_RATING_RANGE(HttpStatus.BAD_REQUEST, "평점은 1에서 5 사이여야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;
}