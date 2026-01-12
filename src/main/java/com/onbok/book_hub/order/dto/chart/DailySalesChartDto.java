package com.onbok.book_hub.order.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 일별 매출 차트 데이터 DTO (Chart.js용)
 */
@Getter
@AllArgsConstructor
public class DailySalesChartDto {
    private List<String> labels;        // 날짜 레이블 (예: ["2024-01-01", "2024-01-02", ...])
    private List<Long> salesData;       // 매출 데이터 (예: [150000, 200000, ...])
    private List<Long> orderCountData;  // 주문 건수 데이터 (예: [5, 8, ...])
}
