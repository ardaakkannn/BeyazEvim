package com.ardakkan.backend.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import com.ardakkan.backend.dto.RefundRequestDto;
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

 // -- Örnek GET /api/refund-requests?status=PENDING --
    @GetMapping
    public ResponseEntity<List<RefundRequestDto>> getRefundRequests(@RequestParam Optional<RefundStatus> status) {
        // Service katmanından RefundRequest listesi çekiliyor
        List<RefundRequest> refundRequests = refundRequestService.getAllRefundRequests(status);
        
        // Entity -> DTO dönüşümü
        List<RefundRequestDto> dtos = refundRequests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // -- Örnek GET /api/refund-requests/user/{userId} --
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RefundRequestDto>> getRefundRequestsByUserId(@PathVariable Long userId) {
        List<RefundRequest> refundRequests = refundRequestService.getRefundRequestsByUserId(userId);

        // Entity -> DTO dönüşümü
        List<RefundRequestDto> dtos = refundRequests.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // -- Örnek POST /api/refund-requests/create/{orderId} --
    @PostMapping("/create/{orderId}")
    public ResponseEntity<?> createRefundRequestForOrder(@PathVariable Long orderId) {
        try {
            refundRequestService.createRefundRequestForOrder(orderId);
            return ResponseEntity.ok("Refund request created successfully.");
        } catch (IllegalStateException e) {
            // 30 gün kuralı, sipariş bulunamaması gibi durumlar
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Diğer beklenmeyen hatalar
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred.");
        }
    }

    // -- Örnek PUT /api/refund-requests/refund-requests/{requestId}/approve?approved=true --
    @PutMapping("/refund-requests/{requestId}/approve")
    public ResponseEntity<String> approveRefundRequest(
            @PathVariable Long requestId,
            @RequestParam boolean approved,
            @AuthenticationPrincipal UserDetails userDetails) {

        refundRequestService.approveRefundRequest(requestId, approved, userDetails);
        return ResponseEntity.ok(approved ? "Refund request approved." : "Refund request rejected.");
    }

    // -----------------------------
    // Private helper method
    // -----------------------------
    private RefundRequestDto convertToDto(RefundRequest entity) {
        RefundRequestDto dto = new RefundRequestDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrder() != null ? entity.getOrder().getId() : null);
        dto.setOrderItemId(entity.getOrderItem() != null ? entity.getOrderItem().getId() : null);
        dto.setProductModelId(entity.getProductModelId());
        dto.setProductInstanceId(entity.getProductInstanceId());
        dto.setStatus(entity.getStatus());
        dto.setRequestedAt(entity.getRequestedAt());
        dto.setApprovedOrRejectedAt(entity.getApprovedOrRejectedAt());

        if (entity.getApprovedBy() != null) {
            dto.setApprovedByUserId(entity.getApprovedBy().getId());
        }
        return dto;
    }
}

