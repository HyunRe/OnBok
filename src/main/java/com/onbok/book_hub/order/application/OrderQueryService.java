package com.onbok.book_hub.order.application;

import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 주문 조회 전용 Service
 */
@Service
@RequiredArgsConstructor
public class OrderQueryService {
    private final OrderRepository orderRepository;

    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.ORDER_NOT_FOUND));
    }

    /**
     * 사용자별 주문 목록 조회
     */
    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * 사용자별 주문 목록과 제목 목록 조회
     */
    public List<String> getOrderTitleList(List<Order> orderList) {
        List<String> orderTitleList = new ArrayList<>();
        for (Order order : orderList) {
            List<OrderItem> orderItems = order.getOrderItems();
            String title = orderItems.get(0).getBook().getTitle();
            int size = orderItems.size();
            if (size > 1) {
                title += " 외 " + (size - 1) + " 건";
            }
            orderTitleList.add(title);
        }
        return orderTitleList;
    }

    /**
     * 기간별 주문 통계 계산
     */
    public OrderStatistics calculateOrderStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Order> orderList = orderRepository.findByOrderDateTimeBetween(startTime, endTime);
        int totalRevenue = 0;
        int totalBooks = 0;

        for (Order order : orderList) {
            totalRevenue += order.getTotalAmount();
            List<OrderItem> orderItems = order.getOrderItems();
            for (OrderItem orderItem : orderItems) {
                totalBooks += orderItem.getQuantity();
            }
        }

        return new OrderStatistics(orderList, totalRevenue, totalBooks);
    }

    public static class OrderStatistics {
        private final List<Order> orderList;
        private final int totalRevenue;
        private final int totalBooks;

        public OrderStatistics(List<Order> orderList, int totalRevenue, int totalBooks) {
            this.orderList = orderList;
            this.totalRevenue = totalRevenue;
            this.totalBooks = totalBooks;
        }

        public List<Order> getOrderList() {
            return orderList;
        }

        public int getTotalRevenue() {
            return totalRevenue;
        }

        public int getTotalBooks() {
            return totalBooks;
        }
    }
}
