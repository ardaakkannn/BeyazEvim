package com.ardakkan.backend.service;



import com.ardakkan.backend.dto.CommentDTO;
import com.ardakkan.backend.dto.UserDTO;
import com.ardakkan.backend.entity.Comment;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.repo.CommentRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final ProductModelRepository productModelRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository, ProductModelRepository productModelRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.productModelRepository = productModelRepository;
        this.userRepository = userRepository;
    }

    public Comment addComment(Long userId, Long productModelId, String title, Integer rating, String text) {
        // Kullanıcıyı bul
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Ürün modelini bul
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));

        // Aynı kullanıcı ve ürün için daha önce yorum bırakılmış mı kontrol et
        if (!commentRepository.findByUserAndProductModel(user, productModel).isEmpty()) {
            throw new RuntimeException("User has already rated this product.");
        }

        // Yeni bir Comment nesnesi oluştur
        Comment comment = new Comment();
        comment.setCreatedDate(LocalDateTime.now());  // Şu anki tarihi ve saati atar
        comment.setTitle(title);
        comment.setRating(rating);
        comment.setText(text);
        comment.setUser(user);
        comment.setProductModel(productModel);
        // text null ise otomatik olarak onaylanır, aksi halde onaysız olur
        if (text == null || text.trim().isEmpty()) {
            comment.setApproved(true); // Text boşsa veya null ise direkt onayla
        } else {
            comment.setApproved(false); // Text varsa onaysız başlasın
        }

        // Yorumunu kaydet
        Comment savedComment = commentRepository.save(comment);

        // Ürün modelinin popülerliğini güncelle
        updateProductPopularity(productModel);
        updateProductRating(productModel);
        return savedComment;
    }
    
    
    private void updateProductRating(ProductModel productModel) {
        // Tüm yorumları al
        List<Comment> comments = commentRepository.findByProductModel(productModel);

        // Ortalama rating'i hesapla
        double averageRating = comments.stream()
                .mapToInt(Comment::getRating)
                .average()
                .orElse(0.0);

        // Ürünün rating'ini güncelle
        productModel.setRating(averageRating);

        // Ürün modelini kaydet
        productModelRepository.save(productModel);
    }
    
    private void updateProductPopularity(ProductModel productModel) {
        // Popülerlik hesapla
        double newPopularity = calculatePopularity(productModel.getId());

        // Popülerlik değerini güncelle
        productModel.setPopularity(newPopularity);

        // Ürün modelini kaydet
        productModelRepository.save(productModel);
    }


    // Tüm yorumları getir
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    // Belirli bir yorum ID'sine göre getir
    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
    }

    // Belirli bir kullanıcıya ait yorumları getir
    public List<Comment> getCommentsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return commentRepository.findByUser(user);
    }

 // Belirli bir ürün modeline ait onaylanmış yorumları getir
    public List<Comment> getCommentsByProductModel(Long productModelId) {
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));
        return commentRepository.findByProductModelAndApproved(productModel, true);
    }


    // Yorum güncelle
    public Comment updateComment(Long commentId, Comment updatedComment) {
        Comment existingComment = getCommentById(commentId);
        existingComment.setTitle(updatedComment.getTitle());
        existingComment.setText(updatedComment.getText());
        existingComment.setRating(updatedComment.getRating());
        return commentRepository.save(existingComment);
    }

    // Yorum onayla veya onayı kaldır
    public Comment approveComment(Long commentId, boolean isApproved) {
        Comment comment = getCommentById(commentId);
        comment.setApproved(isApproved);
        return commentRepository.save(comment);
    }

    // Yorum sil
    public void deleteComment(Long commentId) {
        Comment comment = getCommentById(commentId);
        commentRepository.delete(comment);
    }
    
    // Onaylanmış yorumları getir
    public List<Comment> getApprovedComments() {
        return commentRepository.findByApproved(true);
    }
    
    // Onaylanmamış yorumları getir
    public List<Comment> getUnapprovedComments() {
        return commentRepository.findByApproved(false);
    }


    
    // Popülerlik hesapla
    public double calculatePopularity(Long productModelId) {
        // Ürün modelini al
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));

        // Ürüne ait yorumları al
        List<Comment> comments = commentRepository.findByProductModel(productModel);

        if (comments.isEmpty()) {
            return 0.0; // Yorum yoksa popülerlik sıfırdır
        }

        // Ortalama rating'i hesapla
        double averageRating = comments.stream()
                .mapToInt(Comment::getRating)
                .average()
                .orElse(0.0);

        // Yorum sayısını al ve logaritmik ağırlık uygula
        int totalComments = comments.size();
        return averageRating + Math.log(totalComments + 1); // Ağırlık eklenmiş popülerlik skoru
    }
    
    
    public CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setTitle(comment.getTitle());
        dto.setRating(comment.getRating());
        dto.setText(comment.getText());
        dto.setApproved(comment.getApproved());
        dto.setCreatedDate(comment.getCreatedDate().toString());

        UserDTO userDTO = new UserDTO();
        userDTO.setId(comment.getUser().getId());
        userDTO.setFirstName(comment.getUser().getFirstName());
        userDTO.setLastName(comment.getUser().getLastName());
        userDTO.setEmail(comment.getUser().getEmail());
        userDTO.setAddress(comment.getUser().getAddress());
        userDTO.setPhoneNumber(comment.getUser().getPhoneNumber());
        userDTO.setRole(comment.getUser().getRole());
       
        dto.setUser(userDTO);
        return dto;
    }

}

