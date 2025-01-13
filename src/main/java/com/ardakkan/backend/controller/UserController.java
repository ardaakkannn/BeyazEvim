package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.ProductModelDTO;
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

    
    // Kullanıcının adresini güncelle
    @PutMapping("/{userId}/address")
    public ResponseEntity<UserDTO> updateUserAddress(@PathVariable Long userId, @RequestBody String newAddress) {
        UserDTO updatedUser = userService.updateUserAddress(userId, newAddress);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // Kullanıcının telefon numarasını güncelle
    @PutMapping("/{userId}/phone")
    public ResponseEntity<UserDTO> updateUserPhoneNumber(@PathVariable Long userId, @RequestBody String newPhoneNumber) {
        UserDTO updatedUser = userService.updateUserPhoneNumber(userId, newPhoneNumber);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
    //Kullanıcının adresini getir
     @GetMapping("/{userId}/address")
    public ResponseEntity<String> getUserAddress(@PathVariable Long userId) {
        String address = userService.getUserAddress(userId);
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

  // Kullanıcı ID'sine göre wishlist'e ürün ekleme
     @PostMapping("/{userId}/wishlist/{productModelId}")
     public ResponseEntity<String> addProductToWishlist(
             @PathVariable Long userId, 
             @PathVariable Long productModelId) {
         try {
             userService.addProductToWishlist(userId, productModelId);
             return new ResponseEntity<>("Ürün başarıyla wishlist'e eklendi.", HttpStatus.OK);
         } catch (IllegalStateException e) {
             return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
     }

     // Kullanıcı ID'sine göre wishlist'ten ürün kaldırma
     @DeleteMapping("/{userId}/wishlist/{productModelId}")
     public ResponseEntity<String> removeProductFromWishlist(
             @PathVariable Long userId, 
             @PathVariable Long productModelId) {
         try {
             userService.removeProductFromWishlist(userId, productModelId);
             return new ResponseEntity<>("Ürün başarıyla wishlist'ten kaldırıldı.", HttpStatus.OK);
         } catch (IllegalStateException e) {
             return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
     }

     // Kullanıcı ID'sine göre wishlist'teki ürünleri listeleme
     @GetMapping("/{userId}/wishlist")
     public ResponseEntity<List<ProductModelDTO>> getWishlist(@PathVariable Long userId) {
         try {
             List<ProductModelDTO> wishlist = userService.getWishlist(userId);
             return new ResponseEntity<>(wishlist, HttpStatus.OK);
         } catch (IllegalStateException e) {
             return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
         }
     }
    
}

