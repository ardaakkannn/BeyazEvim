package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.CommentDTO;
import com.ardakkan.backend.dto.CommentRequest;
import com.ardakkan.backend.entity.Comment;
import com.ardakkan.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/users/{userId}/products/{productModelId}")
    public ResponseEntity<Comment> addComment(
            @PathVariable Long userId, 
            @PathVariable Long productModelId, 
            @RequestBody CommentRequest commentRequest) {
        // Yorum ekleme işlemi
        Comment savedComment = commentService.addComment(
                userId, 
                productModelId, 
                commentRequest.getTitle(), 
                commentRequest.getRating(), 
                commentRequest.getText()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(savedComment);
    }



    // Tüm yorumları getir
    @GetMapping
    public ResponseEntity<List<CommentDTO>> getAllComments() {
        List<Comment> comments = commentService.getAllComments();
        List<CommentDTO> approvedCommentDTOs = comments.stream()
                .map(comment -> commentService.convertToDTO(comment))
                .toList();
        return ResponseEntity.ok(approvedCommentDTOs);
    }
    
    @GetMapping("/approved")
    public ResponseEntity<List<CommentDTO>> getApprovedComments() {
        List<Comment> approvedComments = commentService.getApprovedComments();
        List<CommentDTO> approvedCommentDTOs = approvedComments.stream()
                .map(comment -> commentService.convertToDTO(comment))
                .toList();
        return ResponseEntity.ok(approvedCommentDTOs);
    }


    // Onaylanmamış yorumları getir
    @GetMapping("/unapproved")
    public ResponseEntity<List<CommentDTO>> getUnapprovedComments() {
        List<Comment> unapprovedComments = commentService.getUnapprovedComments();
        List<CommentDTO> unapprovedCommentDTOs = unapprovedComments.stream()
                .map(comment -> commentService.convertToDTO(comment))
                .toList();
        return ResponseEntity.ok(unapprovedCommentDTOs);
    }


    // Belirli bir yorum ID'sine göre getir
    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDTO> getCommentById(@PathVariable Long commentId) {
        Comment comment = commentService.getCommentById(commentId);
        CommentDTO commentdto=commentService.convertToDTO(comment);
        return ResponseEntity.ok(commentdto);
    }

    // Belirli bir kullanıcıya ait yorumları getir
    @GetMapping("/users/{userId}")
    public ResponseEntity<List<Comment>> getCommentsByUser(@PathVariable Long userId) {
        List<Comment> comments = commentService.getCommentsByUser(userId);
        return ResponseEntity.ok(comments);
    }

    // Belirli bir ürün modeline ait yorumları getir
    @GetMapping("/products/{productModelId}")
    public ResponseEntity<List<Comment>> getCommentsByProductModel(@PathVariable Long productModelId) {
        List<Comment> comments = commentService.getCommentsByProductModel(productModelId);
        return ResponseEntity.ok(comments);
    }

    // Yorum güncelle
    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long commentId, 
            @RequestBody Comment updatedComment) {
        Comment updated = commentService.updateComment(commentId, updatedComment);
        return ResponseEntity.ok(updated);
    }

    // Yorum onayla veya onayı kaldır
    @PatchMapping("/{commentId}/approve")
    public ResponseEntity<Comment> approveComment(
            @PathVariable Long commentId, 
            @RequestParam boolean isApproved) {
        Comment approvedComment = commentService.approveComment(commentId, isApproved);
        return ResponseEntity.ok(approvedComment);
    }

    // Yorum sil
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}

