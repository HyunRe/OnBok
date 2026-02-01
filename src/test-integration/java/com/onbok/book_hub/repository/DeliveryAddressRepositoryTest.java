package com.onbok.book_hub.repository;

import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.delivery.domain.repository.DeliveryAddressRepository;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EntityScan(basePackages = "com.onbok.book_hub")
@EnableJpaRepositories(basePackages = "com.onbok.book_hub")
@DisplayName("DeliveryAddress Repository 계층 테스트 - @DataJpaTest")
class DeliveryAddressRepositoryTest {

    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("배송지 저장 - 정상 케이스")
    void saveDeliveryAddress_success() {
        // given
        User user = createUser("test@test.com");

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
        DeliveryAddress savedAddress = deliveryAddressRepository.save(address);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(savedAddress.getId()).isNotNull();
        assertThat(savedAddress.getAlias()).isEqualTo("집");
        assertThat(savedAddress.getRecipientName()).isEqualTo("홍길동");
        assertThat(savedAddress.getZipCode()).isEqualTo("06234");
        assertThat(savedAddress.getUser().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("사용자별 배송지 목록 조회 - findByUserId")
    void findByUserId_success() {
        // given
        User user = createUser("user@test.com");

        DeliveryAddress homeAddress = createDeliveryAddress(user, "집", "홍길동");
        DeliveryAddress officeAddress = createDeliveryAddress(user, "회사", "김철수");
        DeliveryAddress parentAddress = createDeliveryAddress(user, "친정", "부모님");

        entityManager.flush();
        entityManager.clear();

        // when
        List<DeliveryAddress> addresses = deliveryAddressRepository.findByUserId(user.getId());

        // then
        assertThat(addresses).hasSize(3);
        assertThat(addresses).extracting("alias")
                .containsExactlyInAnyOrder("집", "회사", "친정");
    }

    @Test
    @DisplayName("사용자와 별칭으로 배송지 조회 - findByUserIdAndAlias")
    void findByUserIdAndAlias_success() {
        // given
        User user = createUser("user@test.com");

        DeliveryAddress homeAddress = createDeliveryAddress(user, "집", "홍길동");
        DeliveryAddress officeAddress = createDeliveryAddress(user, "회사", "김철수");

        entityManager.flush();
        entityManager.clear();

        // when
        Optional<DeliveryAddress> foundAddress = deliveryAddressRepository.findByUserIdAndAlias(user.getId(), "집");

        // then
        assertThat(foundAddress).isPresent();
        assertThat(foundAddress.get().getAlias()).isEqualTo("집");
        assertThat(foundAddress.get().getRecipientName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("배송지 ID로 조회 - findById")
    void findById_success() {
        // given
        User user = createUser("user@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "테스트 주소", "홍길동");

        entityManager.flush();
        entityManager.clear();

        // when
        DeliveryAddress foundAddress = deliveryAddressRepository.findById(address.getId()).orElse(null);

        // then
        assertThat(foundAddress).isNotNull();
        assertThat(foundAddress.getId()).isEqualTo(address.getId());
        assertThat(foundAddress.getAlias()).isEqualTo("테스트 주소");
    }

    @Test
    @DisplayName("배송지 수정 - update")
    void updateDeliveryAddress_success() {
        // given
        User user = createUser("user@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "집", "홍길동");

        entityManager.flush();
        entityManager.clear();

        // when
        DeliveryAddress foundAddress = deliveryAddressRepository.findById(address.getId()).orElseThrow();
        foundAddress.update("회사", "김철수", "12345", "서울시 중구", "456호", "010-9876-5432", "경비실에 맡겨주세요");

        entityManager.flush();
        entityManager.clear();

        // then
        DeliveryAddress updatedAddress = deliveryAddressRepository.findById(address.getId()).orElseThrow();
        assertThat(updatedAddress.getAlias()).isEqualTo("회사");
        assertThat(updatedAddress.getRecipientName()).isEqualTo("김철수");
        assertThat(updatedAddress.getZipCode()).isEqualTo("12345");
        assertThat(updatedAddress.getDetailAddress()).isEqualTo("456호");
    }

    @Test
    @DisplayName("배송지 삭제 - deleteById")
    void deleteDeliveryAddress_success() {
        // given
        User user = createUser("user@test.com");
        DeliveryAddress address = createDeliveryAddress(user, "삭제할 주소", "홍길동");
        Long addressId = address.getId();

        entityManager.flush();
        entityManager.clear();

        // when
        deliveryAddressRepository.deleteById(addressId);
        entityManager.flush();
        entityManager.clear();

        // then
        Optional<DeliveryAddress> deletedAddress = deliveryAddressRepository.findById(addressId);
        assertThat(deletedAddress).isEmpty();
    }

    @Test
    @DisplayName("여러 사용자가 각자 배송지를 가질 수 있음")
    void multipleUsersWithAddresses() {
        // given
        User user1 = createUser("user1@test.com");
        User user2 = createUser("user2@test.com");

        createDeliveryAddress(user1, "user1 집", "홍길동");
        createDeliveryAddress(user1, "user1 회사", "홍길동");
        createDeliveryAddress(user2, "user2 집", "김철수");

        entityManager.flush();
        entityManager.clear();

        // when
        List<DeliveryAddress> user1Addresses = deliveryAddressRepository.findByUserId(user1.getId());
        List<DeliveryAddress> user2Addresses = deliveryAddressRepository.findByUserId(user2.getId());

        // then
        assertThat(user1Addresses).hasSize(2);
        assertThat(user2Addresses).hasSize(1);
        assertThat(user1Addresses).extracting("alias").containsExactlyInAnyOrder("user1 집", "user1 회사");
        assertThat(user2Addresses).extracting("alias").containsExactly("user2 집");
    }

    @Test
    @DisplayName("배송지 전화번호 형식 저장 확인")
    void saveDeliveryAddress_telFormat() {
        // given
        User user = createUser("user@test.com");

        DeliveryAddress address = createDeliveryAddress(user, "집", "홍길동");
        address.updateTel("010-1234-5678");
        deliveryAddressRepository.save(address);

        entityManager.flush();
        entityManager.clear();

        // when
        DeliveryAddress foundAddress = deliveryAddressRepository.findById(address.getId()).orElseThrow();

        // then
        assertThat(foundAddress.getTel()).isEqualTo("010-1234-5678");
    }

    @Test
    @DisplayName("배송 요청사항 포함 저장")
    void saveDeliveryAddress_withDeliveryRequest() {
        // given
        User user = createUser("user@test.com");

        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .alias("집")
                .recipientName("홍길동")
                .zipCode("06234")
                .basicAddress("서울시 강남구")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .memo("부재 시 경비실에 맡겨주세요")
                .build();

        deliveryAddressRepository.save(address);

        entityManager.flush();
        entityManager.clear();

        // when
        DeliveryAddress foundAddress = deliveryAddressRepository.findById(address.getId()).orElseThrow();

        // then
        assertThat(foundAddress.getMemo()).isEqualTo("부재 시 경비실에 맡겨주세요");
    }

    // === Helper Methods ===

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .uname("테스트 유저")
                .build();
        return userRepository.save(user);
    }

    private DeliveryAddress createDeliveryAddress(User user, String alias, String recipientName) {
        DeliveryAddress address = DeliveryAddress.builder()
                .user(user)
                .alias(alias)
                .recipientName(recipientName)
                .zipCode("06234")
                .basicAddress("서울시 강남구 테헤란로")
                .detailAddress("123호")
                .tel("010-1234-5678")
                .memo("문 앞에 놓아주세요")
                .build();
        return deliveryAddressRepository.save(address);
    }
}
