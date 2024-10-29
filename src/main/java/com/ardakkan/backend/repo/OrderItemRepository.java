package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Belirli bir siparişin içindeki tüm ürünleri bulmak
    List<OrderItem> findByOrderId(Long orderId);
    Optional<OrderItem> findByOrderAndProductModelId(Order order, Long productModelId);
   }
