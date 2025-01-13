package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // Belirli bir siparişin içindeki tüm ürünleri bulmak
    List<OrderItem> findByOrderId(Long orderId);
    
    Optional<OrderItem> findByOrderAndProductModelId(Order order, Long productModelId);
    
    boolean existsByProductInstanceIdsContains(Long productInstanceId);
    
    @Query("SELECT oi FROM OrderItem oi JOIN oi.productInstanceIds pi WHERE pi = :productInstanceId")
    List<OrderItem> findByProductInstanceId(@Param("productInstanceId") Long productInstanceId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.productModelId = :productModelId AND oi.order.status = 'IN_CART'")
    List<OrderItem> findByProductModelIdAndInCartOrders(@Param("productModelId") Long productModelId);

   }
