package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.ProductModelDTO;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.service.ProductModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-models")
public class ProductModelController {

    private final ProductModelService productModelService;

    @Autowired
    public ProductModelController(ProductModelService productModelService) {
        this.productModelService = productModelService;
    }
    
    
    // Toplu ProductModel ekleme
    @PostMapping("/bulk")
    public ResponseEntity<List<ProductModel>> saveAllProductModels(@RequestBody List<ProductModel> productModels) {
        List<ProductModel> createdProductModels = productModelService.saveAllProductModels(productModels);
        return new ResponseEntity<>(createdProductModels, HttpStatus.CREATED);
    }

    // Yeni bir ProductModel oluştur - Entity döner
    @PostMapping
    public ResponseEntity<ProductModel> createProductModel(@RequestBody ProductModel productModel) {
        ProductModel createdProductModel = productModelService.createProductModel(productModel);
        return new ResponseEntity<>(createdProductModel, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModelDTO> getProductModelById(@PathVariable Long id) {
        Optional<ProductModelDTO> productModelDTO = productModelService.getProductModelDTOById(id);
        return productModelDTO.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
    

    // Tüm ProductModel'leri getir - Entity döner
    @GetMapping
    public ResponseEntity<List<ProductModelDTO>> getAllProductModels() {
        List<ProductModelDTO> productModelsdto = productModelService.getAllProductModelsDTO();
        return new ResponseEntity<>(productModelsdto, HttpStatus.OK);
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
    
    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<ProductModelDTO>> searchProductModels(@PathVariable String searchString) {
        if (searchString == null || searchString.trim().isEmpty()) {
            return ResponseEntity.badRequest().build(); // Boş arama sorguları için hata döndür
        }
        List<ProductModelDTO> productModels = productModelService.searchProductModels(searchString.trim());
        return ResponseEntity.ok(productModels);
    }
    
    @PostMapping("/{productId}/discount/{discountRate}")
    public ResponseEntity<String> applyDiscount(
            @PathVariable Long productId,
            @PathVariable Double discountRate) {
        try {
            productModelService.organizeDiscount(productId, discountRate);
            return ResponseEntity.ok("Discount of " + discountRate + "% applied successfully to Product with ID: " + productId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/{productModelId}/stock")
    public ResponseEntity<?> addStock(
            @PathVariable Long productModelId,
            @RequestBody Map<String, Integer> request) {

        int quantityToAdd = request.getOrDefault("quantityToAdd", 0);
        if (quantityToAdd <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "quantityToAdd must be greater than 0"
            ));
        }

        try {
            productModelService.addStock(productModelId, quantityToAdd);
            int updatedStock = productModelService.getAvailableProductInstanceCount(productModelId);

            return ResponseEntity.ok(Map.of(
                    "message", quantityToAdd + " units of stock successfully added to ProductModel with ID: " + productModelId,
                    "updatedStock", updatedStock
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/{productModelId}/decrease-stock")
    public ResponseEntity<String> decreaseStock(
            @PathVariable Long productModelId,
            @RequestParam int quantityToRemove) {
        try {
            productModelService.decreaseStock(productModelId, quantityToRemove);
            return ResponseEntity.ok("Stock successfully decreased for product model ID: " + productModelId);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
    
    
    @PutMapping("/{productModelId}/price")
    public ResponseEntity<String> updateProductPrice(
            @PathVariable Long productModelId,
            @RequestParam Double newPrice) {
        productModelService.updateProductPrice(productModelId, newPrice);
        return ResponseEntity.ok("Product price updated, and associated cart orders adjusted successfully.");
    }

    
    
}
