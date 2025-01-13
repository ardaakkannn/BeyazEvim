package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.OrderDTO;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import com.ardakkan.backend.dto.OrderItemDTO;
import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderStatus;
import com.ardakkan.backend.service.OrderService;
import com.ardakkan.backend.service.RefundRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RefundRequestService refundRequestService;

    @Autowired
    public OrderController(OrderService orderService, RefundRequestService refundRequestService) {
        this.orderService = orderService;
        this.refundRequestService=refundRequestService;
    }

    // Yeni sipariş oluşturma
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        Order createdOrder = orderService.createOrder(order);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    // Sipariş ID'si ile sipariş bulma (DTO olarak döner)
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO orderDTO = orderService.findOrderById(id);
        return ResponseEntity.ok(orderDTO);
    }

    // Tüm siparişleri listeleme (DTO olarak döner)
    @GetMapping("/all")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orderDTOs = orderService.getAllOrders();
        return ResponseEntity.ok(orderDTOs);
    }

    //Userın tüm orderlerini getirir
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Long userId) {
        List<OrderDTO> orderDTOs = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orderDTOs);
    }

    /*
    // Kullanıcının sepetindeki ürünleri ve miktarlarını getirme
    @GetMapping("/{userId}/cart")
    public ResponseEntity<List<OrderItemDTO>> getUserCart(@PathVariable Long userId) {
        List<OrderItemDTO> cartItems = orderService.getUserCart(userId);
        return ResponseEntity.ok(cartItems);
    }
    */
   
    @GetMapping("/{userId}/cart")
    public ResponseEntity<OrderDTO> getUserCart(@PathVariable Long userId) {
        OrderDTO cart = orderService.getUserCart(userId);
        return ResponseEntity.ok(cart);
    }
    
    
    
    // Sipariş güncelleme
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody Order updatedOrder) {
        Order order = orderService.updateOrder(id, updatedOrder);
        return ResponseEntity.ok(order);
    }

    // Yeni sepet oluşturma
    @PostMapping("/user/{userId}/create-cart")
    public ResponseEntity<Void> createNewCart(@PathVariable Long userId) {
        orderService.createNewCart(userId);
        return ResponseEntity.ok().build();
    }

    // Sipariş silme
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.ok().build();
    }
    
  //Siparişi satın alma
    @PostMapping("/purchase/{orderId}")
    public ResponseEntity<byte[]> purchaseCart(@PathVariable Long orderId) {
        try {
            // Call the updated purchaseCartItems method, which returns the PDF data as a ResponseEntity
            return orderService.purchaseCartItems(orderId);
        } catch (Exception e) {
            // Handle errors and return an appropriate error message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
                    .body(("Error: " + e.getMessage()).getBytes());
        }
    }
    
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok("Order has been successfully canceled and refund will be processed within 5-10 business days.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    
    @PutMapping("/{orderId}/refund/{productModelId}")
    public ResponseEntity<String> refundOrder(@PathVariable Long orderId, @PathVariable Long productModelId) {
        try {
            orderService.refundOrderItemProduct(orderId,productModelId);
            return ResponseEntity.ok("Refund processed successfully.");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {

        // Request Body'den status bilgisini al
        String status = request.get("status");
        if (status == null || status.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Status is required"
            ));
        }

        try {
            // Enum'a dönüştür
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus);

            return ResponseEntity.ok(Map.of(
                    "message", "Order status updated successfully",
                    "orderId", updatedOrder.getId(),
                    "newStatus", updatedOrder.getStatus().toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid status value. Allowed values: CART, PURCHASED, SHIPPED, DELIVERED, RETURNED, CANCELED"
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "An unexpected error occurred: " + e.getMessage()
            ));
        }
    }
    
    
    @PostMapping("/{orderId}/refund-request")
    public ResponseEntity<String> createRefundRequest(
            @PathVariable Long orderId,
            @RequestParam Long productModelId) {

        refundRequestService.createRefundRequest(orderId, productModelId);
        return ResponseEntity.ok("Refund request created and awaiting approval.");
    }
    
   
     

}
