package com.onbok.book_hub.common.exception;

import com.onbok.book_hub.common.response.OnBokResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ExpectedException.class)
    public ResponseEntity<?> handleExpectedException(ExpectedException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        // SSE 요청인 경우 JSON 응답 대신 빈 응답 반환
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/event-stream")) {
            log.warn("SSE 요청 중 예외 발생: {}, errorCode: {}", ex.getMessage(), errorCode);
            // SSE 요청에서는 JSON을 반환할 수 없으므로 NO_CONTENT 반환
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 응답 헤더에는 400 코드가 가되,
                .body(OnBokResponse.error(errorCode)); // 실제 상태 코드는 ErrorCode의 상태 코드이다.
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<OnBokResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("](은)는 ");
            builder.append(fieldError.getDefaultMessage());
            builder.append(" 입력된 값: [");
            builder.append(fieldError.getRejectedValue());
            builder.append("]");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(OnBokResponse.error(HttpStatus.BAD_REQUEST, builder.toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OnBokResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("잘못된 인자 예외: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(OnBokResponse.error(HttpStatus.BAD_REQUEST, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OnBokResponse<Void>> handleUnExpectedException(Exception ex) {
        log.error("에러 발생 :", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(OnBokResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}