package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Comment;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Belirli bir ürün modeline ait yorumları bulmak
    List<Comment> findByProductModel(ProductModel productModel);

    // Belirli bir kullanıcıya ait yorumları bulmak
    List<Comment> findByUser(User user);
    
}