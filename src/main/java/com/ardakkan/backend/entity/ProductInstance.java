package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "ProductInstance")  // Veritabanındaki tablo adı
public class ProductInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String serialNumber;
    

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductInstanceStatus status;
    
    

    // Many-to-One ilişki: Her ProductInstance bir ProductModel'e aittir
    @ManyToOne
    @JoinColumn(name = "productModel_id", nullable = false)  // Veritabanındaki foreign key alanı
    private ProductModel productModel;

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long idProductInstance) {
        this.id = idProductInstance;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

   

    public ProductInstanceStatus getStatus() {
		return status;
	}

	public void setStatus(ProductInstanceStatus status) {
		this.status = status;
	}

	public ProductModel getProductModel() {
        return productModel;
    }

    public void setProductModel(ProductModel productModel) {
        this.productModel = productModel;
    }
}
