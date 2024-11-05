package com.ardakkan.backend.dto;


public class RegisterRequest {
    private String FirstName;
    private String LastName;
    private String password;
    private String email;

    // Getter ve Setter'lar
   
    public String getPassword() {
        return password;
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
	public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
