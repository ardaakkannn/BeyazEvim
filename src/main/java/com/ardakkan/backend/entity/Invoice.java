package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "Invoice")  // Veritabanındaki tablo adı
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private Date createdAt;

    @Column(length = 345)
    private String details;

    // Many-to-One ilişki: Bir fatura bir kullanıcıya bağlı olabilir
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Veritabanındaki foreign key alanı
    private User user;

    @OneToOne(mappedBy = "invoice")
    private Order order;
    

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long idInvoice) {
        this.id = idInvoice;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
}

