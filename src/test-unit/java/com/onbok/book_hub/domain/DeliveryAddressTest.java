package com.onbok.book_hub.domain;

import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.user.domain.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DisplayName("DeliveryAddress 도메인 단위 테스트")
class DeliveryAddressTest {

    @Test
    @DisplayName("배송지 생성 - 정상 케이스")
    void createDeliveryAddress_success() {
        // given
        User user = User.builder()
                .email("test@test.com")
                .build();

        // when
        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .alias("집")
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .memo("문 앞에 놓아주세요")
                .build();

        // then
        assertThat(address.getAlias()).isEqualTo("집");
        assertThat(address.getRecipientName()).isEqualTo("홍길동");
        assertThat(address.getZipCode()).isEqualTo("06234");
        assertThat(address.getBasicAddress()).isEqualTo("서울시 강남구 테헤란로");
        assertThat(address.getDetailAddress()).isEqualTo("123호");
        assertThat(address.getTel()).isEqualTo("010-1234-5678");
        assertThat(address.getMemo()).isEqualTo("문 앞에 놓아주세요");
    }

    @Test
    @DisplayName("배송지 정보 수정 - 정상 케이스")
    void updateDeliveryAddress_success() {
        // given
        User user = User.builder().email("test@test.com").build();
        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .alias("집")
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .memo("문 앞에 놓아주세요")
                .build();

        // when
        address.update("회사", "김철수", "12345", "서울시 종로구", "456호", "010-9876-5432", "경비실에 맡겨주세요");

        // then
        assertThat(address.getAlias()).isEqualTo("회사");
        assertThat(address.getRecipientName()).isEqualTo("김철수");
        assertThat(address.getZipCode()).isEqualTo("12345");
        assertThat(address.getBasicAddress()).isEqualTo("서울시 종로구");
        assertThat(address.getDetailAddress()).isEqualTo("456호");
        assertThat(address.getTel()).isEqualTo("010-9876-5432");
        assertThat(address.getMemo()).isEqualTo("경비실에 맡겨주세요");
    }

    @Test
    @DisplayName("배송지 생성 시 필수값만 입력 (memo 제외)")
    void createDeliveryAddress_withoutOptionalFields() {
        // given
        User user = User.builder().email("test@test.com").build();

        // when
        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .alias("집")
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .build();

        // then
        assertThat(address.getMemo()).isNull();
        assertThat(address.getAlias()).isEqualTo("집");
        assertThat(address.getRecipientName()).isEqualTo("홍길동");
    }
}
