package com.ardakkan.backend.controller;

import com.ardakkan.backend.entity.Category;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Category> addCategory(@RequestBody Category category) {
        Category newCategory = categoryService.addCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    
    
 // Belirli bir kategorinin alt kategorilerini getir
    @GetMapping("/{categoryId}/subcategories")
    public ResponseEntity<List<Category>> getSubCategories(@PathVariable Long categoryId) {
        List<Category> subCategories = categoryService.getSubCategories(categoryId);
        return ResponseEntity.ok(subCategories);
    }


    // Tüm kategorileri getir
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    // Belirli bir ID ile kategori getir
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    // Bir kategori güncelle
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category updatedCategory) {
        Category category = categoryService.updateCategory(id, updatedCategory);
        return ResponseEntity.ok(category);
    }

    // Bir kategori sil
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Belirli bir kategorideki tüm ürün modellerini getir
    @GetMapping("/{categoryId}/productModels")
    public ResponseEntity<List<ProductModel>> getProductModelsByCategory(@PathVariable Long categoryId) {
        List<ProductModel> productModels = categoryService.getProductModelsByCategory(categoryId);
        return ResponseEntity.ok(productModels);
    }
    
    // Ana kategorileri getiren endpoint
    @GetMapping("/root")
    public ResponseEntity<List<Category>> getRootCategories() {
        List<Category> rootCategories = categoryService.getRootCategories();
        return ResponseEntity.ok(rootCategories);
    }

    // Belirli bir kategoriye yeni ürün modeli ekle
    @PostMapping("/{categoryId}/productModels")
    public ResponseEntity<ProductModel> addProductModelToCategory(
            @PathVariable Long categoryId, 
            @RequestBody ProductModel productModel) {
        ProductModel savedProductModel = categoryService.addProductModelToCategory(categoryId, productModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProductModel);
    }
}
