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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ProductModelRepository productModelRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, ProductModelRepository productModelRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productModelRepository = productModelRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalStateException("Email zaten kayıtlı.");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

        User user = new User();
        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());
        user.setPassword(encodedPassword);
        user.setEmail(registerRequest.getEmail());
        user.setRole(UserRoles.ROLE_CUSTOMER);

        userRepository.save(user);
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
