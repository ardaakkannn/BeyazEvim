package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.entity.ProductModel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable; // Pageable için



@Repository
public interface ProductInstanceRepository extends JpaRepository<ProductInstance, Long> {
    // Belirli bir ürün modeline ait tüm ürün örneklerini bulmak

    Optional<ProductInstance> findFirstByProductModelIdAndStatus(Long productModelId, ProductInstanceStatus status);
    List<ProductInstance> findByStatus(ProductInstanceStatus status);
    
    Optional<ProductInstance> findBySerialNumber(String serialNumber);

    List<ProductInstance> findByProductModel(ProductModel productModel);
 
    List<ProductInstance> findByProductModelAndStatus(ProductModel productModel, ProductInstanceStatus status);
    
    
    
    List<ProductInstance> findAllByProductModelIdAndStatus(Long productModelId, ProductInstanceStatus status);
    
    List<ProductInstance> findAllByProductModelIdAndStatusIn(Long productModelId, List<ProductInstanceStatus> statuses);
    
    
    
    @Query("SELECT pi FROM ProductInstance pi WHERE pi.productModel.id = :productModelId AND pi.status = :status ORDER BY pi.id ASC")
    List<ProductInstance> findTopNByProductModelIdAndStatus(@Param("productModelId") Long productModelId,
                                                           @Param("status") ProductInstanceStatus status,
                                                           Pageable pageable);



    
}
