package com.ardakkan.backend.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "refund_requests")
public class RefundRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    @Column(nullable = false)
    private Long productModelId; // Refund yapılacak ürünün modeli

    @Column(nullable = false)
    private Long productInstanceId; // Refund yapılacak ürün örneği

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status; // PENDING, APPROVED, REJECTED

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_or_rejected_at")
    private LocalDateTime approvedOrRejectedAt;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy; // Onayı veren kullanıcı (Product Manager)

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public OrderItem getOrderItem() {
		return orderItem;
	}

	public void setOrderItem(OrderItem orderItem) {
		this.orderItem = orderItem;
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

	public User getApprovedBy() {
		return approvedBy;
	}

	public void setApprovedBy(User approvedBy) {
		this.approvedBy = approvedBy;
	}
    
    
}
