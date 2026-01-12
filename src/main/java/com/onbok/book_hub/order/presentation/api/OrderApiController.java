package com.onbok.book_hub.order.presentation.api;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.dto.DeliveryAddressResponseDto;
import com.onbok.book_hub.order.application.OrderStatisticsService;
import com.onbok.book_hub.order.dto.chart.CategorySalesChartDto;
import com.onbok.book_hub.order.dto.chart.DailySalesChartDto;
import com.onbok.book_hub.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {
    private final DeliveryAddressService deliveryAddressService;
    private final OrderStatisticsService orderStatisticsService;

    @Operation(summary = "배송지 저장", description = "주문 과정에서 새로운 배송지 정보를 저장합니다")
    @PostMapping("/delivery-address")
    public OnBokResponse<DeliveryAddressResponseDto> saveDeliveryAddress(@CurrentUser User user,
                                                                           @RequestBody DeliveryAddress deliveryAddress) {
        DeliveryAddress savedAddress = deliveryAddressService.insertDeliveryAddress(user, deliveryAddress);
        DeliveryAddressResponseDto data = new DeliveryAddressResponseDto(savedAddress.getId(), "배송지가 저장되었습니다.");
        return OnBokResponse.success(data, HttpStatus.CREATED);
    }

    @Operation(summary = "내 배송지 목록 조회", description = "현재 로그인한 사용자의 모든 배송지를 조회합니다")
    @GetMapping("/delivery-addresses")
    public OnBokResponse<List<DeliveryAddress>> getMyDeliveryAddresses(@CurrentUser User user) {
        List<DeliveryAddress> addresses = deliveryAddressService.findByUser(user);
        return OnBokResponse.success(addresses);
    }

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
