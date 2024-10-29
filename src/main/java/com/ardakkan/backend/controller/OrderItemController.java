package com.ardakkan.backend.controller;



import com.ardakkan.backend.entity.OrderItem;
import com.ardakkan.backend.service.OrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order-items")
public class OrderItemController {

    private final OrderItemService orderItemService;

    @Autowired
    public OrderItemController(OrderItemService orderItemService) {
        this.orderItemService = orderItemService;
    }

    // Belirli bir sipariş ve ürün modeli ile sepete ürün ekleme
    @PostMapping("/add-to-cart")
    public ResponseEntity<OrderItem> addProductToCart(
            @RequestParam Long orderId,
            @RequestParam Long productModelId) {
        OrderItem addedOrderItem = orderItemService.addProductToCart(orderId, productModelId);
        return ResponseEntity.status(HttpStatus.CREATED).body(addedOrderItem);
    }

}

