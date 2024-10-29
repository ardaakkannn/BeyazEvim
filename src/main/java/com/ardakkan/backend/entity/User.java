package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(nullable = false, length = 45, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 200)
    private String address;

    @Column(name = "tax_id", length = 45, nullable = true)
    private String taxId;  // Tüm kullanıcılar için zorunlu olmayabilir

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRoles role;

    // User'in siparişleri (Order) ile ilişkisi: Bir kullanıcı birçok sipariş verebilir
    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    // User'in faturaları (Invoice) ile ilişkisi: Bir kullanıcı birçok fatura sahibi olabilir
    @OneToMany(mappedBy = "user")
    private List<Invoice> invoices;

    // Kullanıcının favori ürünleri
    @OneToMany
    private List<ProductModel> wishlist;

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRoles getRole() {
        return role;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public List<ProductModel> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<ProductModel> wishlist) {
        this.wishlist = wishlist;
    }
}
