package com.ardakkan.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import jakarta.persistence.JoinColumn;



@Entity
@Table(name = "User")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 45)
    private String firstName;
    
    @Column(nullable = false, length = 45)
    private String lastName;

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
    @Column(nullable = false, length = 20)
    private UserRoles role;

    // User'in siparişleri (Order) ile ilişkisi: Bir kullanıcı birçok sipariş verebilir
    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    // User'in faturaları (Invoice) ile ilişkisi: Bir kullanıcı birçok fatura sahibi olabilir
    @OneToMany(mappedBy = "user")
    private List<Invoice> invoices;

    @ManyToMany
    @JoinTable(
        name = "user_wishlist",  // Name of the join table
        joinColumns = @JoinColumn(name = "user_id"),  // Column referencing User entity
        inverseJoinColumns = @JoinColumn(name = "product_id")  // Column referencing ProductModel entity
    )
    private List<ProductModel> wishlist;

   

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    

    public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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
