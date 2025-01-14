package com.ardakkan.backend.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ardakkan.backend.entity.RefundRequest;
import com.ardakkan.backend.entity.RefundStatus;
import com.ardakkan.backend.service.RefundRequestService;

@RestController
@RequestMapping("/api/refund-requests")
public class RefundRequestController {

    private final RefundRequestService refundRequestService;

    @Autowired
    public RefundRequestController(RefundRequestService refundRequestService) {
        this.refundRequestService = refundRequestService;
    }

    @GetMapping
    public ResponseEntity<List<RefundRequest>> getRefundRequests(@RequestParam Optional<RefundStatus> status) {
        List<RefundRequest> refundRequests = refundRequestService.getAllRefundRequests(status);
        return ResponseEntity.ok(refundRequests);
    }
    
    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> createRefundRequestForOrder(@PathVariable Long orderId) {
        try {
            refundRequestService.createRefundRequestForOrder(orderId);
            return ResponseEntity.ok("Refund request created successfully.");
        } catch (IllegalStateException e) {
            // 30 gün kuralı, sipariş bulunamaması veya tek ürün olmaması gibi durumlarda burada yakalanır
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Diğer beklenmeyen hataları burada yakalayabilirsiniz
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }
    
    
    @PutMapping("/refund-requests/{requestId}/approve")
    public ResponseEntity<String> approveRefundRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @AuthenticationPrincipal UserDetails userDetails) {

        refundRequestService.approveRefundRequest(requestId, approved, userDetails);
        return ResponseEntity.ok(approved ? "Refund request approved." : "Refund request rejected.");
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RefundRequest>> getRefundRequestsByUserId(@PathVariable Long userId) {
        List<RefundRequest> refundRequests = refundRequestService.getRefundRequestsByUserId(userId);
        return ResponseEntity.ok(refundRequests);
    }
}

