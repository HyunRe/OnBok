package com.onbok.book_hub.order.application;

import com.onbok.book_hub.book.application.service.book.BookStockService;
import com.onbok.book_hub.cart.domain.model.Cart;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.delivery.application.DeliveryAddressService;
import com.onbok.book_hub.delivery.domain.model.DeliveryAddress;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.model.OrderStatus;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.payment.domain.model.TossPayment;
import com.onbok.book_hub.user.domain.model.User;
import com.onbok.book_hub.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 주문 생성/수정/삭제 관련 비즈니스 로직 (Command)
 */
@Service
@RequiredArgsConstructor
public class OrderCommandService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookStockService bookStockService;
    private final DeliveryAddressService deliveryAddressService;
    private final OrderQueryService orderQueryService;

    @Transactional
    public Order createOrder(Long userId, List<Cart> cartList, TossPayment tossPayment, DeliveryAddress address) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 1. 배송지 정보 저장
        DeliveryAddress savedAddress = deliveryAddressService.insertDeliveryAddress(user, address);

        // 2. 주문 항목(OrderItem) 리스트 먼저 생성
        List<OrderItem> orderItems = new ArrayList<>();
        for (Cart cart : cartList) {
            // 재고 감소 로직
            bookStockService.decreaseStock(cart.getBook().getId(), cart.getQuantity());

            OrderItem orderItem = OrderItem.builder()
                    .book(cart.getBook())
                    .quantity(cart.getQuantity())
                    .subPrice(cart.getBook().getPrice() * cart.getQuantity())
                    .build();
            orderItems.add(orderItem);
        }

        // 3. 주문 생성 (빌더 사용)
        Order order = Order.builder()
                .user(user)
                .totalAmount(tossPayment.getTotalPayment())
                .deliveryAddress(savedAddress)
                .tossPayment(tossPayment)
                .orderItems(orderItems)
                .build();

        return orderRepository.save(order);
    }

    @Transactional
    public void changeOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderQueryService.findById(id);
        order.changeStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void completePayment(Long id) {
        Order order = orderQueryService.findById(id);
        order.completePayment();
        orderRepository.save(order);
    }

    @Transactional
    public void startPreparing(Long id) {
        Order order = orderQueryService.findById(id);
        order.startPreparing();
        orderRepository.save(order);
    }

    @Transactional
    public void shipOrder(Long id) {
        Order order = orderQueryService.findById(id);
        order.ship();
        orderRepository.save(order);
    }

    @Transactional
    public void deliverOrder(Long id) {
        Order order = orderQueryService.findById(id);
        order.deliver();
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long id) {
        Order order = orderQueryService.findById(id);
        order.cancel();
        // 재고 복구
        for (OrderItem item : order.getOrderItems()) {
            bookStockService.increaseStock(item.getBook().getId(), item.getQuantity());
        }
        orderRepository.save(order);
    }

    @Transactional
    public void refundOrder(Long id) {
        Order order = orderQueryService.findById(id);
        order.refund();
        // 재고 복구
        for (OrderItem item : order.getOrderItems()) {
            bookStockService.increaseStock(item.getBook().getId(), item.getQuantity());
        }
        orderRepository.save(order);
    }
}
