package com.onbok.book_hub.delivery.presentation.api;

import com.onbok.book_hub.common.annotation.CurrentUser;
import com.onbok.book_hub.common.response.OnBokResponse;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.dto.DeliveryAddressDto;
import com.onbok.book_hub.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "DeliveryAddress", description = "배송지 관리 API")
@RestController
@RequestMapping("/api/delivery-addresses")
@RequiredArgsConstructor
public class DeliveryAddressApiController {
    private final DeliveryAddressService deliveryAddressService;

    @Operation(summary = "배송지 목록 조회")
    @GetMapping
    public OnBokResponse<List<DeliveryAddressDto>> getMyDeliveryAddresses(@CurrentUser User user) {
        List<DeliveryAddressDto> addresses = deliveryAddressService.getUserDeliveryAddresses(user);
        return OnBokResponse.success(addresses);
    }

    @Operation(summary = "배송지 상세 조회")
    @GetMapping("/{id}")
    public OnBokResponse<DeliveryAddressDto> getDeliveryAddress(@PathVariable Long id) {
        DeliveryAddressDto address = deliveryAddressService.getDeliveryAddressDto(id);
        return OnBokResponse.success(address);
    }

    @Operation(summary = "배송지 등록")
    @PostMapping
    public OnBokResponse<DeliveryAddressDto> createDeliveryAddress(@CurrentUser User user,
                                                                    @RequestBody DeliveryAddressDto dto) {
        var createdAddress = deliveryAddressService.createDeliveryAddress(user, dto);
        return OnBokResponse.success(DeliveryAddressDto.from(createdAddress));
    }

    @Operation(summary = "배송지 수정")
    @PutMapping("/{id}")
    public OnBokResponse<String> updateDeliveryAddress(@CurrentUser User user,
                                                       @PathVariable Long id,
                                                       @RequestBody DeliveryAddressDto dto) {
        deliveryAddressService.updateDeliveryAddress(user, id, dto);
        return OnBokResponse.success("배송지가 수정되었습니다.");
    }

    @Operation(summary = "배송지 삭제")
    @DeleteMapping("/{id}")
    public OnBokResponse<String> deleteDeliveryAddress(@CurrentUser User user,
                                                       @PathVariable Long id) {
        deliveryAddressService.deleteDeliveryAddress(user, id);
        return OnBokResponse.success("배송지가 삭제되었습니다.");
    }
}
