package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Kategori adını kullanarak kategori bulmak
    Category findByCategoryName(String categoryName);
    List<Category> findByParentCategoryIsNull();
    List<Category> findAllByIsActiveTrue(); // Sadece aktif kategoriler
    List<Category> findByParentCategoryIsNullAndIsActiveTrue(); // Sadece aktif olan ana kategoriler
}
