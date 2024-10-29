package com.ardakkan.backend.service;

import com.ardakkan.backend.dto.UserDTO;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.repo.UserRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Şifre hashleme için
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
    private final PasswordEncoder passwordEncoder; // Şifre hashlemek için

    @Autowired
    public UserService(UserRepository userRepository, ProductModelRepository productModelRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productModelRepository = productModelRepository;
        this.passwordEncoder = passwordEncoder; // PasswordEncoder'ı inject ediyoruz
    }

    // DTO olmadan kullanıcıyı kaydetme işlemi
    public User saveUser(User user) {
        // Aynı email ile başka bir kullanıcı olup olmadığını kontrol ediyoruz
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("Email zaten kullanılıyor: " + user.getEmail());
        }

        // Şifreyi hash'liyoruz
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword); // Hashlenmiş şifreyi kullanıcıya set ediyoruz

        // Kullanıcıyı kaydediyoruz ve direkt olarak User entity dönüyoruz
        return userRepository.save(user);
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
    
 // Kullanıcının wishlist'ini güncelleme
    public void updateWishlist(Long userId, List<Long> wishlistProductModelIds) {
        User user = convertToEntity(findUserById(userId)); // Kullanıcıyı buluyoruz
        List<ProductModel> wishlist = wishlistProductModelIds.stream()
                .map(productModelRepository::findById)  // ID ile ProductModel çekiyoruz
                .map(Optional::get) // Optional'dan çıkarıyoruz (boş gelirse exception atacak)
                .collect(Collectors.toList());
        user.setWishlist(wishlist); // Kullanıcının wishlist'ini güncelliyoruz
        userRepository.save(user);  // Güncellenmiş kullanıcıyı kaydediyoruz
    }

    // Entity'yi DTO'ya dönüştür (wishlist ile birlikte)
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setAddress(user.getAddress());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setRole(user.getRole());

        // ProductModel ID'lerini al
        List<Long> wishlistIds = user.getWishlist()
                .stream()
                .map(ProductModel::getId) // Sadece ID'leri alıyoruz
                .collect(Collectors.toList());
        userDTO.setWishlist(wishlistIds);

        return userDTO;
    }

    // DTO'yu Entity'ye dönüştür (wishlist ile birlikte)
    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setId(userDTO.getId());
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(userDTO.getRole());

        // ProductModel ID'lerinden entity'leri çek
        List<ProductModel> wishlist = userDTO.getWishlist()
                .stream()
                .map(productModelRepository::findById)  // ID ile ProductModel çekiyoruz
                .map(Optional::get) // Optional'dan çıkarıyoruz
                .collect(Collectors.toList());
        user.setWishlist(wishlist);

        return user;
    }
    
}
