package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "Comment")  // Veritabanındaki tablo adı
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String title;  // Şemadaki "Commentcol" alanı

    @Column(nullable = false)
    private Integer rating;  // 1-5 arasında puanlama

    @Column(nullable = false, length = 245)
    private String text;

    @Column(nullable = false)
    private Boolean approved;
    
    // Yeni eklenen tarih alanı
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    // Many-to-One ilişki: Bir yorum bir kullanıcıya aittir
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Veritabanındaki foreign key alanı
    private User user;

    // Many-to-One ilişki: Bir yorum bir ürün modeline aittir
    @ManyToOne
    @JoinColumn(name = "productModel_id", nullable = false)  // Veritabanındaki foreign key alanı
    private ProductModel productModel;

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long idComment) {
        this.id = idComment;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProductModel getProductModel() {
        return productModel;
    }

    public void setProductModel(ProductModel productModel) {
        this.productModel = productModel;
    }

	public LocalDateTime getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDateTime createdDate) {
		this.createdDate = createdDate;
	}
    
}

