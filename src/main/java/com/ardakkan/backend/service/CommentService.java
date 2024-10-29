package com.ardakkan.backend.service;



import com.ardakkan.backend.entity.Comment;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.repo.CommentRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    // Yeni bir yorum ekle
    public Comment addComment(Long userId, Long productModelId, Comment comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));

        comment.setUser(user);
        comment.setProductModel(productModel);
        comment.setApproved(false);  // İlk başta onaysız olabilir
        return commentRepository.save(comment);
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

    // Belirli bir ürün modeline ait yorumları getir
    public List<Comment> getCommentsByProductModel(Long productModelId) {
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));
        return commentRepository.findByProductModel(productModel);
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
}

