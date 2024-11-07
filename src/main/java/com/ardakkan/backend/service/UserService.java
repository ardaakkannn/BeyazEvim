package com.ardakkan.backend.service;

import com.ardakkan.backend.dto.RegisterRequest;
import com.ardakkan.backend.dto.UserDTO;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.entity.UserRoles;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.repo.UserRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProductModelRepository productModelRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    @Autowired
    public UserService(UserRepository userRepository, ProductModelRepository productModelRepository, PasswordEncoder passwordEncoder, OrderService orderService ) {
        this.userRepository = userRepository;
        this.productModelRepository = productModelRepository;
        this.passwordEncoder = passwordEncoder;
        this.orderService=orderService;
    }

    public void registerUser(RegisterRequest registerRequest) {
        // Gerekli alanların dolu olup olmadığını kontrol ediyoruz
        validateRegisterRequest(registerRequest);

        // Emailin zaten kayıtlı olup olmadığını kontrol ediyoruz
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalStateException("Bu email adresi zaten kayıtlı.");
        }

        // Şifreyi encode ediyoruz
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // Yeni kullanıcı oluşturuyoruz
        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(encodedPassword);
        user.setEmail(registerRequest.getEmail());
        user.setRole(UserRoles.CUSTOMER);

        // Kullanıcıyı veritabanına kaydediyoruz
        userRepository.save(user);

        // Kullanıcı oluşturulduktan sonra bir sepet (CART) statüsünde sipariş oluşturuyoruz
        orderService.createNewCart(user.getId());
    }

    private void validateRegisterRequest(RegisterRequest registerRequest) {
        if (registerRequest.getFirstName() == null || registerRequest.getFirstName().isEmpty()) {
            throw new IllegalArgumentException("İsim boş olamaz.");
        }
        if (registerRequest.getLastName() == null || registerRequest.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Soyisim boş olamaz.");
        }
        if (registerRequest.getPassword() == null || registerRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Şifre boş olamaz.");
        }
        if (registerRequest.getEmail() == null || registerRequest.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email boş olamaz.");
        }
    }



    public UserDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı: " + id));

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        
    }
    
    
    public User findByEmail(String email) {
        // User bulunamazsa, bir hata fırlatabiliriz veya Optional olarak dönebiliriz
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + email));
    }
    
    public void updateWishlist(Long userId, List<Long> wishlistProductModelIds) {
        User user = convertToEntity(findUserById(userId));
        List<ProductModel> wishlist = wishlistProductModelIds.stream()
                .map(productModelRepository::findById)
                .map(Optional::get)
                .collect(Collectors.toList());
        user.setWishlist(wishlist);
        userRepository.save(user);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setEmail(user.getEmail());
        userDTO.setAddress(user.getAddress());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());

        List<Long> wishlistIds = user.getWishlist()
                .stream()
                .map(ProductModel::getId)
                .collect(Collectors.toList());
        userDTO.setWishlist(wishlistIds);

        return userDTO;
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());

        List<ProductModel> wishlist = userDTO.getWishlist()
                .stream()
                .map(productModelRepository::findById)
                .map(Optional::get)
                .collect(Collectors.toList());
        user.setWishlist(wishlist);

        return user;
    }
}
