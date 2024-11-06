package com.ardakkan.backend.service;



import com.ardakkan.backend.entity.Category;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.repo.CategoryRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductModelRepository productModelRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductModelRepository productModelRepository) {
        this.categoryRepository = categoryRepository;
        this.productModelRepository = productModelRepository;
    }

    // Yeni bir kategori ekle
    public Category addCategory(Category category) {
        return categoryRepository.save(category);
    }

    // Tüm kategorileri getir
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    // Belirli bir ID ile kategori getir
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }

    // Bir kategori güncelle
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = getCategoryById(id);
        existingCategory.setCategoryName(updatedCategory.getCategoryName());
        return categoryRepository.save(existingCategory);
    }

    // Bir kategori sil
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }

 // Belirli bir kategorideki tüm ürün modellerini getir
    public List<ProductModel> getProductModelsByCategory(Long categoryId) {
        return productModelRepository.findByCategoryId(categoryId);
    }
    
    
    // Belirli bir kategoriye yeni ürün modeli ekle
    public ProductModel addProductModelToCategory(Long categoryId, ProductModel productModel) {
        Category category = getCategoryById(categoryId);
        productModel.setCategory(category);
        return productModelRepository.save(productModel);
    }
}

