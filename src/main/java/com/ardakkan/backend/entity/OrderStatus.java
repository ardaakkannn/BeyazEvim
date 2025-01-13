package com.ardakkan.backend.entity;

public enum OrderStatus {
    CART,        // Sepette
    PURCHASED,   // Satın alındı (işlemde)
    SHIPPED,     // Kargoya verildi
    DELIVERED,   // Teslim edildi
    RETURNED,// İade edildi
    CANCELED,
    RETURN_REQUESTED,
    RETURN_REJECTED
}
