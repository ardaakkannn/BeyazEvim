package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "ProductModel")
public class ProductModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(length = 245)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(length = 45)
    private String distributorInformation;
    

    // Ürünün fotoğraf yolunu saklıyoruz
    @Column(name = "photo_path", length = 255)
    private String photoPath;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long idProduct) {
        this.id = idProduct;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDistributorInformation() {
        return distributorInformation;
    }

    public void setDistributorInformation(String distributorInformation) {
        this.distributorInformation = distributorInformation;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}

