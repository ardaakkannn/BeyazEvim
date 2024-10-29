package com.ardakkan.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ardakkan.backend.entity.OrderItem;
import com.ardakkan.backend.entity.OrderStatus;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.repo.OrderItemRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.entity.ProductInstance;
import jakarta.transaction.Transactional;

@Service
@Transactional
public class OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final ProductModelRepository productModelRepository;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository, 
                            OrderRepository orderRepository, 
                            ProductInstanceRepository productInstanceRepository,
                            ProductModelRepository productModelRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productInstanceRepository = productInstanceRepository;
        this.productModelRepository = productModelRepository;
    }

    public OrderItem addProductToCart(Long orderId, Long productModelId) {
        // Order'ı getir
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order bulunamadı: " + orderId));

        // Stokta ya da sepette olan uygun ProductInstance'ı bulun
        Optional<ProductInstance> productInstanceOpt = productInstanceRepository
                .findFirstByProductModelIdAndStatus(productModelId, ProductInstanceStatus.IN_STOCK);

        if (productInstanceOpt.isEmpty()) {
            productInstanceOpt = productInstanceRepository
                    .findFirstByProductModelIdAndStatus(productModelId, ProductInstanceStatus.IN_CART);
        }

        if (productInstanceOpt.isEmpty()) {
            throw new IllegalStateException("Stokta veya sepette uygun ürün bulunamadı: " + productModelId);
        }

        ProductInstance productInstance = productInstanceOpt.get();

        // Order ve ProductModel üzerinden mevcut bir OrderItem olup olmadığını kontrol edin
        Optional<OrderItem> existingOrderItemOpt = orderItemRepository
                .findByOrderAndProductModelId(order, productModelId);

        OrderItem orderItem;
        
        if (existingOrderItemOpt.isPresent()) {
            // Mevcut OrderItem bulundu, miktarı artır
            orderItem = existingOrderItemOpt.get();
            orderItem.setQuantity(orderItem.getQuantity() + 1);
            orderItem.getProductInstances().add(productInstance);
        } else {
            // Yeni OrderItem oluştur
            orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.getProductInstances().add(productInstance);
            orderItem.setQuantity(1);
        }

        // Ürün sepete eklendiği için durumunu IN_CART olarak güncelle
        productInstance.setStatus(ProductInstanceStatus.IN_CART);
        productInstanceRepository.save(productInstance);

        // OrderItem'ı kaydet ve döndür
        return orderItemRepository.save(orderItem);
    }


}

