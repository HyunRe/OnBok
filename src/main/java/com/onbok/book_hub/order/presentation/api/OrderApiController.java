package com.onbok.book_hub.order.presentation.api;

import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.order.application.OrderStatisticsService;
import com.onbok.book_hub.order.dto.chart.CategorySalesChartDto;
import com.onbok.book_hub.order.dto.chart.DailySalesChartDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final OrderStatisticsService orderStatisticsService;

    @Operation(summary = "일별 매출 차트 데이터", description = "최근 N일간의 일별 매출 및 주문 건수 데이터를 조회합니다")
    @GetMapping("/chart/daily-sales")
    public OnBokResponse<DailySalesChartDto> getDailySalesChart(@RequestParam(defaultValue = "7") int days) {
        DailySalesChartDto chartData = orderStatisticsService.getDailySalesChartData(days);
        return OnBokResponse.success(chartData);
    }

    @Operation(summary = "카테고리별 판매 비율 차트 데이터", description = "최근 N일간의 카테고리별 판매 수량 데이터를 조회합니다")
    @GetMapping("/chart/category-sales")
    public OnBokResponse<CategorySalesChartDto> getCategorySalesChart(@RequestParam(defaultValue = "30") int days) {
        CategorySalesChartDto chartData = orderStatisticsService.getCategorySalesChartData(days);
        return OnBokResponse.success(chartData);
    }
}
