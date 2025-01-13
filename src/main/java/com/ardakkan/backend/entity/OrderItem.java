package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long productModelId;

    @Column(nullable = false)
    private Double unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ElementCollection
    @CollectionTable(name = "order_item_product_instance", joinColumns = @JoinColumn(name = "order_item_id"))
    @Column(name = "product_instance_id")
    private List<Long> productInstanceIds = new ArrayList<>(); 
    
    
    @ElementCollection
    @CollectionTable(name = "order_item_returned_instances", joinColumns = @JoinColumn(name = "order_item_id"))
    @Column(name = "product_instance_id")
    private List<Long> returnedProductInstanceIds = new ArrayList<>();

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductModelId() {
        return productModelId;
    }

    public void setProductModelId(Long productModelId) {
        this.productModelId = productModelId;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<Long> getProductInstanceIds() {
        return productInstanceIds;
    }

    public void setProductInstanceIds(List<Long> productInstanceIds) {
        this.productInstanceIds = productInstanceIds;
    }

	public List<Long> getReturnedProductInstanceIds() {
		return returnedProductInstanceIds;
	}

	public void setReturnedProductInstanceIds(List<Long> returnedProductInstanceIds) {
		this.returnedProductInstanceIds = returnedProductInstanceIds;
	}
    
    
    
}
