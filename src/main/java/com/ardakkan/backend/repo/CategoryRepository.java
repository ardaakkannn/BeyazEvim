package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Category;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // Kategori adını kullanarak kategori bulmak
    Category findByCategoryName(String categoryName);
    List<Category> findByParentCategoryIsNull();
}
