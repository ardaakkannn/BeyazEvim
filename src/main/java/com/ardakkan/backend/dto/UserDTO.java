package com.ardakkan.backend.dto;

import com.ardakkan.backend.entity.UserRoles; // UserRoles Enum'ını import ediyoruz
import java.util.List;

public class UserDTO {
    private Long id;
    private String FirstName;
    private String LastName;
    private String email;
    private String address;
    private String phoneNumber;
    private UserRoles role; // Kullanıcı rolü

    // Wishlist artık List<Long> şeklinde, yani sadece ID'leri tutuyor
    private List<Long> wishlist;

    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

   

    public String getFirstName() {
		return FirstName;
	}

	public void setFirstName(String firstName) {
		FirstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}

	public void setLastName(String lastName) {
		LastName = lastName;
	}

	public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<Long> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<Long> wishlist) {
        this.wishlist = wishlist;
    }

    public UserRoles getRole() {
        return role;
    }

    public void setRole(UserRoles role) {
        this.role = role;
    }
}
