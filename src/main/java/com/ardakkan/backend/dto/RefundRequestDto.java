package com.ardakkan.backend.dto;


import java.time.LocalDateTime;

import com.ardakkan.backend.entity.RefundStatus;

public class RefundRequestDto {

    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long productModelId;
    private Long productInstanceId;
    private RefundStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedOrRejectedAt;
    private Long approvedByUserId; // Onayı veren User'ın ID'si

    // --- Getter ve Setter'lar ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Long orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Long getProductModelId() {
        return productModelId;
    }

    public void setProductModelId(Long productModelId) {
        this.productModelId = productModelId;
    }

    public Long getProductInstanceId() {
        return productInstanceId;
    }

    public void setProductInstanceId(Long productInstanceId) {
        this.productInstanceId = productInstanceId;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedOrRejectedAt() {
        return approvedOrRejectedAt;
    }

    public void setApprovedOrRejectedAt(LocalDateTime approvedOrRejectedAt) {
        this.approvedOrRejectedAt = approvedOrRejectedAt;
    }

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public void setApprovedByUserId(Long approvedByUserId) {
        this.approvedByUserId = approvedByUserId;
    }
}
