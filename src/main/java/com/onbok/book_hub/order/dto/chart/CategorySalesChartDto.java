package com.onbok.book_hub.order.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * 카테고리별 판매 비율 차트 데이터 DTO (Chart.js Pie/Doughnut용)
 */
@Getter
@AllArgsConstructor
public class CategorySalesChartDto {
    private List<String> labels;    // 카테고리 레이블 (예: ["소설", "IT/컴퓨터", "자기계발"])
    private List<Long> data;        // 판매 수량 또는 매출 (예: [120, 85, 67])
}
