package com.onbok.book_hub.order.application;

import com.onbok.book_hub.book.dto.BookStateDto;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.order.dto.chart.CategorySalesChartDto;
import com.onbok.book_hub.order.dto.chart.DailySalesChartDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 주문 통계 관련 비즈니스 로직
 */
@Service
@RequiredArgsConstructor
public class OrderStatisticsService {
    private final OrderRepository orderRepository;

    /**
     * 기간별 도서 판매 통계 생성
     */
    public List<BookStateDto> calculateBookStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        // 1. 해당 기간의 주문 목록 조회
        List<Order> orderList = orderRepository.findByOrderDateTimeBetween(startTime, endTime);
        Map<Long, BookStateDto> map = new HashMap<>();

        for (Order order : orderList) {
            for (OrderItem item : order.getOrderItems()) {
                long bid = item.getBook().getId();

                if (map.containsKey(bid)) {
                    // 2. 이미 맵에 있다면, 내부 메서드(addQuantity)를 호출하여 수량과 총액을 함께 갱신
                    map.get(bid).addQuantity(item.getQuantity());
                } else {
                    // 3. 처음 등장한 책이라면 Builder로 생성 (생성자 내부에서 초기 totalPrice 계산됨)
                    BookStateDto bookStat = BookStateDto.builder()
                            .id(bid)
                            .title(item.getBook().getTitle())
                            .company(item.getBook().getCompany())
                            .unitPrice(item.getBook().getPrice())
                            .quantity(item.getQuantity())
                            .build();
                    map.put(bid, bookStat);
                }
            }
        }

        // 4. 별도의 totalPrice 계산 루프 없이 바로 리스트로 변환하여 반환
        return new ArrayList<>(map.values());
    }

    /**
     * 일별 매출 차트 데이터 생성 (최근 N일)
     */
    public DailySalesChartDto getDailySalesChartData(int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        List<Order> orders = orderRepository.findByOrderDateTimeBetween(startTime, endTime);

        // 날짜별로 그룹화
        Map<LocalDate, Long> dailySales = new HashMap<>();
        Map<LocalDate, Long> dailyOrderCount = new HashMap<>();

        for (Order order : orders) {
            LocalDate date = order.getOrderDateTime().toLocalDate();
            dailySales.merge(date, (long) order.getTotalAmount(), Long::sum);
            dailyOrderCount.merge(date, 1L, Long::sum);
        }

        // 최근 N일 날짜 레이블 생성 (빈 날짜도 포함)
        List<String> labels = new ArrayList<>();
        List<Long> salesData = new ArrayList<>();
        List<Long> orderCountData = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.toString());
            salesData.add(dailySales.getOrDefault(date, 0L));
            orderCountData.add(dailyOrderCount.getOrDefault(date, 0L));
        }

        return new DailySalesChartDto(labels, salesData, orderCountData);
    }

    /**
     * 카테고리별 판매 비율 차트 데이터 생성
     */
    public CategorySalesChartDto getCategorySalesChartData(int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        List<Order> orders = orderRepository.findByOrderDateTimeBetween(startTime, endTime);

        // 카테고리별로 판매 수량 집계
        Map<String, Long> categoryQuantity = new HashMap<>();

        for (Order order : orders) {
            for (OrderItem item : order.getOrderItems()) {
                String category = item.getBook().getCategory();
                if (category == null || category.isEmpty()) {
                    category = "미분류";
                }
                categoryQuantity.merge(category, (long) item.getQuantity(), Long::sum);
            }
        }

        // Chart.js 형식으로 변환
        List<String> labels = new ArrayList<>(categoryQuantity.keySet());
        List<Long> data = labels.stream()
                .map(categoryQuantity::get)
                .collect(Collectors.toList());

        return new CategorySalesChartDto(labels, data);
    }
}
