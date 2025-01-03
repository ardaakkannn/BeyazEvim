package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Belirli bir kullanıcının tüm siparişlerini bulmak
    List<Order> findByUserId(Long userId);
    Optional<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}
