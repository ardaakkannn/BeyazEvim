package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.entity.ProductModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductInstanceRepository extends JpaRepository<ProductInstance, Long> {
    // Belirli bir ürün modeline ait tüm ürün örneklerini bulmak

    Optional<ProductInstance> findFirstByProductModelIdAndStatus(Long productModelId, ProductInstanceStatus status);
    List<ProductInstance> findByStatus(ProductInstanceStatus status);
    
    Optional<ProductInstance> findBySerialNumber(String serialNumber);

    List<ProductInstance> findByProductModel(ProductModel productModel);
 
    List<ProductInstance> findByProductModelAndStatus(ProductModel productModel, ProductInstanceStatus status);
}
