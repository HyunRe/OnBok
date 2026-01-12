package com.onbok.book_hub.recommendation.application;

import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.repository.book.BookRepository;
import com.onbok.book_hub.common.exception.ErrorCode;
import com.onbok.book_hub.common.exception.ExpectedException;
import com.onbok.book_hub.order.domain.model.Order;
import com.onbok.book_hub.order.domain.model.OrderItem;
import com.onbok.book_hub.order.domain.repository.OrderRepository;
import com.onbok.book_hub.review.domain.model.Review;
import com.onbok.book_hub.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    // 사용자의 주문 내역 기반 추천
    public List<Book> getRecommendationsByOrderHistory(Long userId, int limit) {
        List<Order> orders = orderRepository.findByUserIdOrderByIdDesc(userId);

        // 사용자가 구매한 도서 목록
        Set<Long> purchasedBookIds = orders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> item.getBook().getId())
                .collect(Collectors.toSet());

        if (purchasedBookIds.isEmpty()) {
            // 구매 이력이 없으면 인기 도서 반환
            return getPopularBooks(limit);
        }

        // 같은 저자의 다른 책들 추천
        Map<Long, Integer> bookScores = new HashMap<>();

        for (Long bookId : purchasedBookIds) {
            Book book = bookRepository.findById(bookId).orElseThrow(() -> new ExpectedException(ErrorCode.BOOK_NOT_FOUND));
            if (book == null) continue;

            // 같은 저자의 다른 책들
            List<Book> sameAuthorBooks = bookRepository.findByAuthor(book.getAuthor());
            for (Book sameAuthorBook : sameAuthorBooks) {
                if (!purchasedBookIds.contains(sameAuthorBook.getId())) {
                    bookScores.put(sameAuthorBook.getId(),
                            bookScores.getOrDefault(sameAuthorBook.getId(), 0) + 3);
                }
            }

            // 같은 출판사의 다른 책들
            List<Book> sameCompanyBooks = bookRepository.findByCompany(book.getCompany());
            for (Book sameCompanyBook : sameCompanyBooks) {
                if (!purchasedBookIds.contains(sameCompanyBook.getId())) {
                    bookScores.put(sameCompanyBook.getId(),
                            bookScores.getOrDefault(sameCompanyBook.getId(), 0) + 1);
                }
            }
        }

        // 점수 순으로 정렬하여 추천
        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> bookRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 사용자의 리뷰 기반 추천 (높은 평점을 준 책과 유사한 책)
    public List<Book> getRecommendationsByReviews(Long userId, int limit) {
        List<Review> reviews = reviewRepository.findByUserIdOrderByCreatedAtDesc(userId);

        // 평점 4점 이상을 준 도서만 필터링
        Set<Long> likedBookIds = reviews.stream()
                .filter(review -> review.getRating() >= 4)
                .map(review -> review.getBook().getId())
                .collect(Collectors.toSet());

        if (likedBookIds.isEmpty()) {
            return getHighlyRatedBooks(limit);
        }

        // 좋아한 책들과 유사한 책 추천
        Map<Long, Integer> bookScores = new HashMap<>();

        for (Long bookId : likedBookIds) {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book == null) continue;

            // 같은 저자의 다른 책들
            List<Book> sameAuthorBooks = bookRepository.findByAuthor(book.getAuthor());
            for (Book sameAuthorBook : sameAuthorBooks) {
                if (!likedBookIds.contains(sameAuthorBook.getId())) {
                    bookScores.put(sameAuthorBook.getId(),
                            bookScores.getOrDefault(sameAuthorBook.getId(), 0) + 5);
                }
            }
        }

        return bookScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> bookRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 협업 필터링: 비슷한 구매 패턴을 가진 사용자들이 구매한 책 추천
    public List<Book> getCollaborativeFilteringRecommendations(Long userId, int limit) {
        List<Order> myOrders = orderRepository.findByUserIdOrderByIdDesc(userId);

        // 내가 구매한 도서 ID 목록
        Set<Long> myBookIds = myOrders.stream()
                .flatMap(order -> order.getOrderItems().stream())
                .map(item -> item.getBook().getId())
                .collect(Collectors.toSet());

        if (myBookIds.isEmpty()) {
            return getPopularBooks(limit);
        }

        // 나와 비슷한 책을 구매한 사용자 찾기
        List<Order> allOrders = orderRepository.findAll();
        Map<Long, Integer> similarUserScores = new HashMap<>();

        for (Order order : allOrders) {
            Long otherUserId = order.getUser().getId();
            if (otherUserId.equals(userId)) continue;

            Set<Long> otherBookIds = order.getOrderItems().stream()
                    .map(item -> item.getBook().getId())
                    .collect(Collectors.toSet());

            // 공통으로 구매한 책의 개수로 유사도 측정
            long commonBooks = myBookIds.stream()
                    .filter(otherBookIds::contains)
                    .count();

            if (commonBooks > 0) {
                similarUserScores.put(otherUserId, similarUserScores.getOrDefault(otherUserId, 0) + (int) commonBooks);
            }
        }

        // 유사 사용자들이 구매한 책 중 내가 구매하지 않은 책 추천
        Map<Long, Integer> recommendedBooks = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : similarUserScores.entrySet()) {
            Long similarUserId = entry.getKey();
            int similarity = entry.getValue();

            List<Order> similarUserOrders = orderRepository.findByUserIdOrderByIdDesc(similarUserId);
            for (Order order : similarUserOrders) {
                for (OrderItem item : order.getOrderItems()) {
                    Long bookId = item.getBook().getId();
                    if (!myBookIds.contains(bookId)) {
                        recommendedBooks.put(bookId,
                                recommendedBooks.getOrDefault(bookId, 0) + similarity);
                    }
                }
            }
        }

        return recommendedBooks.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> bookRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 인기 도서 (주문이 많은 순)
    public List<Book> getPopularBooks(int limit) {
        List<Order> allOrders = orderRepository.findAll();
        Map<Long, Integer> bookOrderCounts = new HashMap<>();

        for (Order order : allOrders) {
            for (OrderItem item : order.getOrderItems()) {
                Long bookId = item.getBook().getId();
                bookOrderCounts.put(bookId,
                        bookOrderCounts.getOrDefault(bookId, 0) + item.getQuantity());
            }
        }

        return bookOrderCounts.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> bookRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 평점이 높은 도서
    public List<Book> getHighlyRatedBooks(int limit) {
        List<Review> allReviews = reviewRepository.findAll();
        Map<Long, List<Integer>> bookRatings = new HashMap<>();

        for (Review review : allReviews) {
            Long bookId = review.getBook().getId();
            bookRatings.computeIfAbsent(bookId, k -> new ArrayList<>()).add(review.getRating());
        }

        // 평균 평점 계산 및 정렬
        Map<Long, Double> avgRatings = new HashMap<>();
        for (Map.Entry<Long, List<Integer>> entry : bookRatings.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0);
            if (entry.getValue().size() >= 3) {  // 최소 3개 이상의 리뷰가 있는 책만
                avgRatings.put(entry.getKey(), avg);
            }
        }

        return avgRatings.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> bookRepository.findById(entry.getKey()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
