package com.onbok.book_hub.delivery;

import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.dto.DeliveryAddressDto;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@DisplayName("DeliveryAddress 통합 테스트")
class DeliveryAddressIntegrationTest {

    @Autowired
    private DeliveryAddressService deliveryAddressService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("배송지 등록 - 정상 케이스")
    void createDeliveryAddress_success() {
        // given
        User user = createUser("test@test.com");
        DeliveryAddressDto dto = DeliveryAddressDto.builder()
                .alias("집")
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .memo("문 앞에 놓아주세요")
                .build();

        // when
        DeliveryAddress address = deliveryAddressService.createDeliveryAddress(user, dto);

        // then
        assertThat(address.getId()).isNotNull();
        assertThat(address.getAlias()).isEqualTo("집");
        assertThat(address.getRecipientName()).isEqualTo("홍길동");
        assertThat(address.getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("사용자의 배송지 목록 조회")
    void getUserDeliveryAddresses_success() {
        // given
        User user = createUser("test@test.com");
        createDeliveryAddress(user, "집");
        createDeliveryAddress(user, "회사");
        createDeliveryAddress(user, "우리집");

        // when
        List<DeliveryAddressDto> addresses = deliveryAddressService.getUserDeliveryAddresses(user);

        // then
        assertThat(addresses).hasSize(3);
        assertThat(addresses)
                .extracting("alias")
                .containsExactlyInAnyOrder("집", "회사", "우리집");
    }

    @Test
    @DisplayName("배송지 수정 - 정상 케이스")
    void updateDeliveryAddress_success() {
        // given
        User user = createUser("test@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "집");

        DeliveryAddressDto updateDto = DeliveryAddressDto.builder()
                .alias("회사")
                .recipientName("김철수")
                .zipCode("12345")
                .basicAddress("서울시 종로구")
                .detailAddress("456호")
                .tel("010-9876-5432")
                .memo("경비실에 맡겨주세요")
                .build();

        // when
        deliveryAddressService.updateDeliveryAddress(user, address.getId(), updateDto);

        // then
        DeliveryAddress updatedAddress = deliveryAddressService.findById(address.getId());
        assertThat(updatedAddress.getAlias()).isEqualTo("회사");
        assertThat(updatedAddress.getRecipientName()).isEqualTo("김철수");
        assertThat(updatedAddress.getZipCode()).isEqualTo("12345");
    }

    @Test
    @DisplayName("배송지 수정 실패 - 다른 사용자의 배송지")
    void updateDeliveryAddress_unauthorized() {
        // given
        User owner = createUser("owner@test.com");
        User otherUser = createUser("other@test.com");
        DeliveryAddress address = createDeliveryAddress(owner, "집");

        DeliveryAddressDto updateDto = DeliveryAddressDto.builder()
                .alias("해킹 시도")
                .recipientName("해커")
                .zipCode("00000")
                .basicAddress("해킹 주소")
                .detailAddress("000호")
                .tel("000-0000-0000")
                .build();

        // when & then
        assertThatThrownBy(() ->
                deliveryAddressService.updateDeliveryAddress(otherUser, address.getId(), updateDto)
        ).isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("배송지 삭제 - 정상 케이스")
    void deleteDeliveryAddress_success() {
        // given
        User user = createUser("test@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "집");
        Long addressId = address.getId();

        // when
        deliveryAddressService.deleteDeliveryAddress(user, addressId);

        // then
        assertThatThrownBy(() ->
                deliveryAddressService.findById(addressId)
        ).isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("배송지 삭제 실패 - 다른 사용자의 배송지")
    void deleteDeliveryAddress_unauthorized() {
        // given
        User owner = createUser("owner@test.com");
        User otherUser = createUser("other@test.com");
        DeliveryAddress address = createDeliveryAddress(owner, "집");

        // when & then
        assertThatThrownBy(() ->
                deliveryAddressService.deleteDeliveryAddress(otherUser, address.getId())
        ).isInstanceOf(ExpectedException.class);
    }

    @Test
    @DisplayName("사용자별 배송지 개수 조회")
    void countByUser_success() {
        // given
        User user = createUser("test@test.com");
        createDeliveryAddress(user, "집");
        createDeliveryAddress(user, "회사");

        // when
        long count = deliveryAddressService.countByUser(user);

        // then
        assertThat(count).isEqualTo(2);
    }

    // === Helper Methods ===

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .uname("테스트 유저")
                .build();
        return userRepository.save(user);
    }

    private DeliveryAddress createDeliveryAddress(User user, String alias) {
        DeliveryAddressDto dto = DeliveryAddressDto.builder()
                .alias(alias)
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .build();
        return deliveryAddressService.createDeliveryAddress(user, dto);
    }
}
