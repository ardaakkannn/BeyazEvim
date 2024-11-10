package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.ProductModelDTO;
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

    // Yeni bir ProductModel oluştur - Entity döner
    @PostMapping
    public ResponseEntity<ProductModel> createProductModel(@RequestBody ProductModel productModel) {
        ProductModel createdProductModel = productModelService.createProductModel(productModel);
        return new ResponseEntity<>(createdProductModel, HttpStatus.CREATED);
    }

    // ID ile ProductModel getir - DTO döner
    @GetMapping("/{id}")
    public ResponseEntity<ProductModelDTO> getProductModelById(@PathVariable Long id) {
        Optional<ProductModelDTO> productModelDTO = productModelService.getProductModelDTOById(id);
        return productModelDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Tüm ProductModel'leri getir - Entity döner
    @GetMapping
    public ResponseEntity<List<ProductModel>> getAllProductModels() {
        List<ProductModel> productModels = productModelService.getAllProductModels();
        return new ResponseEntity<>(productModels, HttpStatus.OK);
    }

    // Belirli bir marka ile ProductModel'leri ara - DTO döner
    @GetMapping("/search/brand")
    public ResponseEntity<List<ProductModelDTO>> getProductModelsByBrand(@RequestParam String brand) {
        List<ProductModelDTO> productModels = productModelService.getProductModelsDTOByBrand(brand);
        return new ResponseEntity<>(productModels, HttpStatus.OK);
    }

    // Belirli bir ad ile ProductModel'leri ara - DTO döner
    @GetMapping("/search/name")
    public ResponseEntity<List<ProductModelDTO>> getProductModelsByName(@RequestParam String name) {
        List<ProductModelDTO> productModels = productModelService.getProductModelsDTOByName(name);
        return new ResponseEntity<>(productModels, HttpStatus.OK);
    }

    // ProductModel güncelleme - Entity döner
    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> updateProductModel(@PathVariable Long id, @RequestBody ProductModel productModelDetails) {
        ProductModel updatedProductModel = productModelService.updateProductModel(id, productModelDetails);
        return new ResponseEntity<>(updatedProductModel, HttpStatus.OK);
    }

    // ProductModel silme - Entity döner
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductModel(@PathVariable Long id) {
        productModelService.deleteProductModel(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Belirli bir ProductModel'in toplam ProductInstance sayısını getir - Entity döner
    @GetMapping("/{productModelId}/instances/count")
    public ResponseEntity<Integer> getProductInstanceCount(@PathVariable Long productModelId) {
        int count = productModelService.getProductInstanceCount(productModelId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    // Belirli bir ProductModel'in AVAILABLE durumda olan ProductInstance sayısını getir - Entity döner
    @GetMapping("/{productModelId}/instances/available-count")
    public ResponseEntity<Integer> getAvailableProductInstanceCount(@PathVariable Long productModelId) {
        int availableCount = productModelService.getAvailableProductInstanceCount(productModelId);
        return new ResponseEntity<>(availableCount, HttpStatus.OK);
    }

    // Rastgele 16 ProductModel getir - DTO döner
    @GetMapping("/random")
    public ResponseEntity<List<ProductModelDTO>> getRandomProductModels() {
        List<ProductModelDTO> randomProducts = productModelService.getRandomProductModels();
        return new ResponseEntity<>(randomProducts, HttpStatus.OK);
    }
}
