package com.onbok.book_hub.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
public class ExecutionTimeAspect {
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        // 클래스 명과 메소드 명 가져오기
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = ((MethodSignature) joinPoint.getSignature()).getMethod().getName();

        //  시작 시간
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("Method: " + className + "." + methodName + "() | Start Time: " + startTime);

        Object result = joinPoint.proceed();    // primary concern 실행

        // 종료 시간
        LocalDateTime endTime = LocalDateTime.now();
        System.out.println("Method: " + className + "." + methodName + "() | End Time: " + endTime);

        // 종료 시간 - 시작 시긴 차이 출력
        long duration = java.time.Duration.between(startTime, endTime).toMillis();
        System.out.println("Duration: " + duration + " ms");

        return result;
    }
}
