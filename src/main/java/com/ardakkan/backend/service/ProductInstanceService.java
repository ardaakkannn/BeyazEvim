package com.ardakkan.backend.service;


import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.repo.ProductInstanceRepository;
import com.ardakkan.backend.repo.ProductModelRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductInstanceService {

    private final ProductInstanceRepository productInstanceRepository;
    private final ProductModelRepository productModelRepository;

    @Autowired
    public ProductInstanceService(ProductInstanceRepository productInstanceRepository,
                                  ProductModelRepository productModelRepository) {
        this.productInstanceRepository = productInstanceRepository;
        this.productModelRepository = productModelRepository;
    }

    // Yeni bir ProductInstance ekleme
    public ProductInstance createProductInstance(ProductInstance productInstance, Long productModelId) {
        Optional<ProductModel> productModel = productModelRepository.findById(productModelId);
        if (productModel.isPresent()) {
            productInstance.setProductModel(productModel.get());
            return productInstanceRepository.save(productInstance);
        } else {
            throw new RuntimeException("ProductModel not found with id: " + productModelId);
        }
    }

    // ProductInstance güncelleme
    public ProductInstance updateProductInstance(Long id, ProductInstance productInstance) {
        Optional<ProductInstance> existingProductInstance = productInstanceRepository.findById(id);
        if (existingProductInstance.isPresent()) {
            ProductInstance updatedInstance = existingProductInstance.get();
            updatedInstance.setSerialNumber(productInstance.getSerialNumber());
            updatedInstance.setStatus(productInstance.getStatus());
            updatedInstance.setProductModel(productInstance.getProductModel());
            return productInstanceRepository.save(updatedInstance);
        } else {
            throw new RuntimeException("ProductInstance not found with id: " + id);
        }
    }

    // ID ile ProductInstance alma
    public Optional<ProductInstance> getProductInstanceById(Long id) {
        return productInstanceRepository.findById(id);
    }

    // Tüm ProductInstance'ları listeleme
    public List<ProductInstance> getAllProductInstances() {
        return productInstanceRepository.findAll();
    }

    // Status'e göre ProductInstance listeleme
    public List<ProductInstance> getProductInstancesByStatus(ProductInstanceStatus status) {
        return productInstanceRepository.findByStatus(status);
    }

    // SerialNumber ile ProductInstance bulma
    public Optional<ProductInstance> getProductInstanceBySerialNumber(String serialNumber) {
        return productInstanceRepository.findBySerialNumber(serialNumber);
    }

    // Belirli bir ProductModel'e ait tüm ProductInstance'ları getirme
    public List<ProductInstance> getProductInstancesByProductModel(Long productModelId) {
        Optional<ProductModel> productModel = productModelRepository.findById(productModelId);
        if (productModel.isPresent()) {
            return productInstanceRepository.findByProductModel(productModel.get());
        } else {
            throw new RuntimeException("ProductModel not found with id: " + productModelId);
        }
    }

    // ProductInstance silme
    public void deleteProductInstance(Long id) {
        if (productInstanceRepository.existsById(id)) {
            productInstanceRepository.deleteById(id);
        } else {
            throw new RuntimeException("ProductInstance not found with id: " + id);
        }
    }

    // ProductInstance durumunu güncelleme
    public ProductInstance updateStatus(Long id, ProductInstanceStatus newStatus) {
        Optional<ProductInstance> productInstance = productInstanceRepository.findById(id);
        if (productInstance.isPresent()) {
            ProductInstance instanceToUpdate = productInstance.get();
            instanceToUpdate.setStatus(newStatus);
            return productInstanceRepository.save(instanceToUpdate);
        } else {
            throw new RuntimeException("ProductInstance not found with id: " + id);
        }
    }
    //Toplu Product Instance Ekleme
    public List<ProductInstance> createProductInstances(List<ProductInstance> productInstances, Long productModelId) {
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));

        productInstances.forEach(instance -> instance.setProductModel(productModel));
        return productInstanceRepository.saveAll(productInstances);
    }

    
}

