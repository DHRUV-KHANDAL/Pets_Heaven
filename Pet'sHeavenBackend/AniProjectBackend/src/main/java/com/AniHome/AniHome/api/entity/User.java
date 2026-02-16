package com.AniHome.AniHome.api.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;
import java.util.Set;

@Document(collection = "users")
public class User {
	@Id
	@Pattern(regexp = "^[a-zA-Z]{3,}$", message = "User Name must be 3 letter long")
    private String userName;
	
	@Pattern(regexp = "^[a-zA-Z]{3,}$", message = "First Name must be 3 letter long")
	@Field("userFirstName")
	private String userFirstName;
	
	@Field("userLastName")
	private String userLastName;
	
	@Email(message = "Provide valid email")
	@Field("userEmail")
    private String userEmail;
	
	@Pattern(regexp = "^[6-9]\\d{9}$", message = "Provide Indian phone number")
	@Field("userPhone")
    private String userPhone;

	@Field("city")
	private String city;

	@Field("userPassword")
	private String userPassword;
    
    @Field("userRole")
    private String userRole;
    
    @Field("role")
	private Set<Role> role;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getUserFirstName() {
		return userFirstName;
	}

	public void setUserFirstName(String userFirstName) {
		this.userFirstName = userFirstName;
	}

	public String getUserLastName() {
		return userLastName;
	}

	public void setUserLastName(String userLastName) {
		this.userLastName = userLastName;
	}

  

    public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(String userPhone) {
		this.userPhone = userPhone;
	}

	public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }
    
    public String getUserRole() {
		return userRole;
	}

    public Set<Role> getRole() {
        return role;
    }

    public void setRole(Set<Role> role) {
        this.role = role;
    }
    
    public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
    
}
