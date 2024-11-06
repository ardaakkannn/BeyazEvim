package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.RegisterRequest;
import com.ardakkan.backend.dto.UserDTO;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }



    // ID ile kullanıcıyı getir
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO userDTO = userService.findUserById(id);
        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    // Tüm kullanıcıları listele
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    // Kullanıcının wishlist'ini güncelle
    @PutMapping("/{userId}/wishlist")
    public ResponseEntity<Void> updateUserWishlist(@PathVariable Long userId, @RequestBody List<Long> wishlistProductModelIds) {
        userService.updateWishlist(userId, wishlistProductModelIds);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

