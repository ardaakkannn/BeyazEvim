package com.ardakkan.backend.controller;



import com.ardakkan.backend.dto.ProductModelDTO;
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

    
 // Ürünü sepete ekleme
    @PostMapping("/add")
    public ResponseEntity<OrderItem> addProductToCart(@RequestParam Long orderId, @RequestParam Long productModelId) {
        OrderItem orderItem = orderItemService.addProductToCart(orderId, productModelId);
        return ResponseEntity.ok(orderItem);
    }

    // Ürünü sepetten kaldırma
    @PostMapping("/remove")
    public ResponseEntity<OrderItem> removeProductFromCart(@RequestParam Long orderId, @RequestParam Long productModelId) {
        OrderItem orderItem = orderItemService.removeProductFromCart(orderId, productModelId);
        return ResponseEntity.ok(orderItem);
    }

    // OrderItem ID'sine göre ProductModelDTO döndürme
    @GetMapping("/{orderItemId}/product-model")
    public ResponseEntity<ProductModelDTO> getProductModelByOrderItemId(@PathVariable Long orderItemId) {
        ProductModelDTO productModelDTO = orderItemService.getProductModelByOrderItemId(orderItemId);
        return ResponseEntity.ok(productModelDTO);
    }

}

