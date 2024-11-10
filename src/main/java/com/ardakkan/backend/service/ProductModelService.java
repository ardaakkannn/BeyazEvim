package com.ardakkan.backend.service;


import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.dto.ProductModelDTO;
import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductModelService {

    private final ProductModelRepository productModelRepository;
    private final ProductInstanceRepository productInstanceRepository;

    @Autowired
    public ProductModelService(ProductModelRepository productModelRepository,
                               ProductInstanceRepository productInstanceRepository) {
        this.productModelRepository = productModelRepository;
        this.productInstanceRepository = productInstanceRepository;
    }

    // Yeni bir ProductModel ekleme
    public ProductModel createProductModel(ProductModel productModel) {
        return productModelRepository.save(productModel);
    }

    // ProductModel güncelleme
    public ProductModel updateProductModel(Long id, ProductModel productModelDetails) {
        Optional<ProductModel> existingProductModelOpt = productModelRepository.findById(id);
        if (existingProductModelOpt.isPresent()) {
            ProductModel existingProductModel = existingProductModelOpt.get();
            existingProductModel.setName(productModelDetails.getName());
            existingProductModel.setDistributorInformation(productModelDetails.getDistributorInformation());
            existingProductModel.setDescription(productModelDetails.getDescription());
            existingProductModel.setPrice(productModelDetails.getPrice());
            existingProductModel.setCategory(productModelDetails.getCategory());
            return productModelRepository.save(existingProductModel);
        } else {
            throw new RuntimeException("ProductModel not found with id: " + id);
        }
    }

   
    
   // ID ile ProductModel getirme - DTO döndürür
    public Optional<ProductModelDTO> getProductModelDTOById(Long id) {
        return productModelRepository.findById(id)
                .map(this::convertToDTO);
    }


    // Tüm ProductModel'leri getirme
    public List<ProductModel> getAllProductModels() {
        return productModelRepository.findAll();
    }

 // Belirli bir marka ile arama - DTO döndürür
    public List<ProductModelDTO> getProductModelsDTOByBrand(String brand) {
        return productModelRepository.findByDistributorInformation(brand)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


 // Ürün adı ile arama - DTO döndürür
    public List<ProductModelDTO> getProductModelsDTOByName(String name) {
        return productModelRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ProductModel silme
    public void deleteProductModel(Long id) {
        if (productModelRepository.existsById(id)) {
            productModelRepository.deleteById(id);
        } else {
            throw new RuntimeException("ProductModel not found with id: " + id);
        }
    }

    // Belirli bir ProductModel'in toplam ProductInstance sayısını getirme
    public int getProductInstanceCount(Long productModelId) {
        Optional<ProductModel> productModel = productModelRepository.findById(productModelId);
        if (productModel.isPresent()) {
            return productInstanceRepository.findByProductModel(productModel.get()).size();
        } else {
            throw new RuntimeException("ProductModel not found with id: " + productModelId);
        }
    }

 // Belirli bir ProductModel'in AVAILABLE durumda olan ProductInstance sayısını getirme
    public int getAvailableProductInstanceCount(Long productModelId) {
        Optional<ProductModel> productModel = productModelRepository.findById(productModelId);
        if (productModel.isPresent()) {
            // "IN_STOCK" ve "IN_CART" durumundaki tüm ProductInstance'ları listeye ekleme
            List<ProductInstance> instances = productInstanceRepository.findByProductModelAndStatus(
                    productModel.get(), ProductInstanceStatus.IN_STOCK);
            instances.addAll(productInstanceRepository.findByProductModelAndStatus(
                    productModel.get(), ProductInstanceStatus.IN_CART));
            
            return instances.size();
        } else {
            throw new RuntimeException("ProductModel not found with id: " + productModelId);
        }
    }
    
    public List<ProductModelDTO> getRandomProductModels() {
        List<ProductModel> allProducts = productModelRepository.findAll(); // Tüm ürünleri çek
        Collections.shuffle(allProducts); // Ürünleri karıştır
        
        // İlk 16 ürünü al ve DTO'ya dönüştür
        return allProducts.stream()
                .limit(16)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ProductModel -> ProductModelDTO dönüşümü
    private ProductModelDTO convertToDTO(ProductModel productModel) {
        ProductModelDTO dto = new ProductModelDTO();
        dto.setId(productModel.getId());
        dto.setName(productModel.getName());
        dto.setDescription(productModel.getDescription());
        dto.setPrice(productModel.getPrice());
        return dto;
    }

}

