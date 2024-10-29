package com.ardakkan.backend.repo;


import com.ardakkan.backend.entity.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductModelRepository extends JpaRepository<ProductModel, Long> {
    // Belirli bir kategoriye ait tüm ürün modellerini bulmak
    List<ProductModel> findByCategoryId(Long categoryId);
    List<ProductModel> findByDistributorInformation(String brand);

    List<ProductModel> findByNameContainingIgnoreCase(String name);
}
