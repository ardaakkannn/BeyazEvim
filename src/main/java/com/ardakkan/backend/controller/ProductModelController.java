package com.ardakkan.backend.controller;

import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.service.ProductModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-models")
public class ProductModelController {

    private final ProductModelService productModelService;

    @Autowired
    public ProductModelController(ProductModelService productModelService) {
        this.productModelService = productModelService;
    }

    // Yeni bir ProductModel oluştur
    @PostMapping
    public ResponseEntity<ProductModel> createProductModel(@RequestBody ProductModel productModel) {
        ProductModel createdProductModel = productModelService.createProductModel(productModel);
        return new ResponseEntity<>(createdProductModel, HttpStatus.CREATED);
    }

    // ID ile ProductModel getir
    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> getProductModelById(@PathVariable Long id) {
        Optional<ProductModel> productModel = productModelService.getProductModelById(id);
        return productModel.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Tüm ProductModel'leri getir
    @GetMapping
    public ResponseEntity<List<ProductModel>> getAllProductModels() {
        List<ProductModel> productModels = productModelService.getAllProductModels();
        return new ResponseEntity<>(productModels, HttpStatus.OK);
    }

    // Belirli bir marka ile ProductModel'leri ara
    @GetMapping("/search/brand")
    public ResponseEntity<List<ProductModel>> getProductModelsByBrand(@RequestParam String brand) {
        List<ProductModel> productModels = productModelService.getProductModelsByBrand(brand);
        return new ResponseEntity<>(productModels, HttpStatus.OK);
    }

    // Belirli bir ad ile ProductModel'leri ara
    @GetMapping("/search/name")
    public ResponseEntity<List<ProductModel>> getProductModelsByName(@RequestParam String name) {
        List<ProductModel> productModels = productModelService.getProductModelsByName(name);
        return new ResponseEntity<>(productModels, HttpStatus.OK);
    }

    // ProductModel güncelleme
    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> updateProductModel(@PathVariable Long id, @RequestBody ProductModel productModelDetails) {
        ProductModel updatedProductModel = productModelService.updateProductModel(id, productModelDetails);
        return new ResponseEntity<>(updatedProductModel, HttpStatus.OK);
    }

    // ProductModel silme
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductModel(@PathVariable Long id) {
        productModelService.deleteProductModel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Belirli bir ProductModel'in toplam ProductInstance sayısını getir
    @GetMapping("/{productModelId}/instances/count")
    public ResponseEntity<Integer> getProductInstanceCount(@PathVariable Long productModelId) {
        int count = productModelService.getProductInstanceCount(productModelId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    // Belirli bir ProductModel'in AVAILABLE durumda olan ProductInstance sayısını getir
    @GetMapping("/{productModelId}/instances/available-count")
    public ResponseEntity<Integer> getAvailableProductInstanceCount(@PathVariable Long productModelId) {
        int availableCount = productModelService.getAvailableProductInstanceCount(productModelId);
        return new ResponseEntity<>(availableCount, HttpStatus.OK);
    }
}

