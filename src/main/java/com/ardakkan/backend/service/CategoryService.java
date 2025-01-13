package com.ardakkan.backend.service;



import com.ardakkan.backend.dto.CategoryProductsDTO;
import com.ardakkan.backend.entity.Category;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.repo.CategoryRepository;
import com.ardakkan.backend.repo.ProductModelRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductModelRepository productModelRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductModelRepository productModelRepository) {
        this.categoryRepository = categoryRepository;
        this.productModelRepository = productModelRepository;
    }
    
    
    public Category addCategory(Category category) {
        if (category.getParentCategory() != null) {
            // Eğer parentCategory null değilse, parentCategory'nin geçerli bir kategori olduğundan emin ol
            Category parentCategory = getCategoryById(category.getParentCategory().getId());
            category.setParentCategory(parentCategory);

            // Alt kategoriyi üst kategorinin subCategories listesine ekle
            List<Category> subCategories = parentCategory.getSubCategories();
            subCategories.add(category);
            parentCategory.setSubCategories(subCategories);

            categoryRepository.save(parentCategory); // Üst kategoriyi günceller
        } else {
            // Eğer parentCategory null ise, bu kategori ana kategori olur
            category.setParentCategory(null);
        }
        return categoryRepository.save(category);
    }


    // Belirli bir kategorinin alt kategorilerini getir
    public List<Category> getSubCategories(Long categoryId) {
        Category parentCategory = getCategoryById(categoryId);

        // Alt kategorileri filtreleyerek sadece aktif olanları döndür
        return parentCategory.getSubCategories().stream()
                .filter(Category::isActive) // Sadece isActive = true olanları al
                .collect(Collectors.toList());
    }


    // Tüm kategorileri getir
    public List<Category> getAllCategories() {
    	return categoryRepository.findAllByIsActiveTrue();
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
    
    public CategoryProductsDTO getProductModelsAndBrandsByCategory(Long categoryId) {
        // Önce kategoriyi al
        Category category = getCategoryById(categoryId);

        // Kategori bir root kategori mi, yoksa sub kategori mi?
        boolean isRootCategory = (category.getParentCategory() == null);

        // Tüm ürün modellerini ve markaları toplayacak liste/set
        List<ProductModel> productModels = new ArrayList<>();
        Set<String> uniqueBrands = new HashSet<>();

        if (isRootCategory) {
            // Root kategori ise, alt kategorilerden ürünleri topla
            collectProductsFromCategory(category, productModels, uniqueBrands);
        } else {
            // Sub kategori ise, sadece bu kategoriye ait ürünleri topla
            productModels = productModelRepository.findByCategoryId(categoryId);

            for (ProductModel productModel : productModels) {
                String brand = productModel.getDistributorInformation();
                if (brand != null && !brand.isEmpty()) {
                    uniqueBrands.add(brand);
                }
            }
        }

        // DTO oluştur ve doldur
        CategoryProductsDTO categoryProductsDTO = new CategoryProductsDTO();
        categoryProductsDTO.setProductModels(productModels);
        categoryProductsDTO.setBrands(new ArrayList<>(uniqueBrands));

        return categoryProductsDTO;
    }
    
    private void collectProductsFromCategory(Category category, List<ProductModel> allProducts, Set<String> uniqueBrands) {
        // Kategorideki ürün modellerini topla
        List<ProductModel> products = productModelRepository.findByCategoryId(category.getId());
        allProducts.addAll(products);

        for (ProductModel productModel : products) {
            String brand = productModel.getDistributorInformation();
            if (brand != null && !brand.isEmpty()) {
                uniqueBrands.add(brand);
            }
        }

        // Alt kategorileri dolaşarak ürünleri topla
        for (Category subCategory : category.getSubCategories()) {
            collectProductsFromCategory(subCategory, allProducts, uniqueBrands);
        }
    }


    @Transactional
    public void deactivateCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);

        // Kategoriyi etkisizleştir
        category.setActive(false);
        categoryRepository.save(category);

        // Bu kategoriye bağlı tüm ürün modellerini etkisizleştir
        List<ProductModel> productModels = productModelRepository.findByCategoryId(categoryId);
        for (ProductModel productModel : productModels) {
            productModel.setActive(false);
            productModelRepository.save(productModel);
        }

        // Alt kategoriler için aynı işlemi yap
        for (Category subCategory : category.getSubCategories()) {
            deactivateCategory(subCategory.getId());
        }
    }


    
    
    // Belirli bir kategoriye yeni ürün modeli ekle
    public ProductModel addProductModelToCategory(Long categoryId, ProductModel productModel) {
        Category category = getCategoryById(categoryId);
        productModel.setCategory(category);
        return productModelRepository.save(productModel);
    }
    
  // Ana kategorileri getirme (parentCategory=null olanlar)
    public List<Category> getRootCategories() {
    	return categoryRepository.findByParentCategoryIsNullAndIsActiveTrue();
    }
    
   
    
}

