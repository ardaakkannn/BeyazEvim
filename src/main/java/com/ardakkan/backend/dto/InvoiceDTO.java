package com.ardakkan.backend.dto;

import java.time.LocalDateTime;

public class InvoiceDTO {
    private Long id;
    private Double totalPrice;
    private LocalDateTime createdAt;
    private String details;
    private Long userId;  // User detayını nested olarak göndermek yerine sadece ID tutuyoruz.
    private Long orderId; // Order detayını nested olarak göndermek yerine sadece ID tutuyoruz.

    public InvoiceDTO() {
    }

    public InvoiceDTO(Long id, Double totalPrice, LocalDateTime createdAt, String details, Long userId, Long orderId) {
        this.id = id;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.details = details;
        this.userId = userId;
        this.orderId = orderId;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}

