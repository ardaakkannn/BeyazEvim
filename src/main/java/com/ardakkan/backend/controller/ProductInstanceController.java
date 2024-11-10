package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.ProductInstanceRequest;
import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.service.ProductInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product-instances")
public class ProductInstanceController {

    private final ProductInstanceService productInstanceService;

    @Autowired
    public ProductInstanceController(ProductInstanceService productInstanceService) {
        this.productInstanceService = productInstanceService;
    }

    // Yeni ProductInstance oluştur
    @PostMapping
    public ResponseEntity<ProductInstance> createProductInstance(@RequestBody ProductInstance productInstance,
                                                                 @RequestParam Long productModelId) {
        ProductInstance createdInstance = productInstanceService.createProductInstance(productInstance, productModelId);
        return new ResponseEntity<>(createdInstance, HttpStatus.CREATED);
    }

    // ID'ye göre ProductInstance al
    @GetMapping("/{id}")
    public ResponseEntity<ProductInstance> getProductInstanceById(@PathVariable Long id) {
        Optional<ProductInstance> productInstance = productInstanceService.getProductInstanceById(id);
        return productInstance.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Tüm ProductInstance'ları listele
    @GetMapping
    public ResponseEntity<List<ProductInstance>> getAllProductInstances() {
        List<ProductInstance> instances = productInstanceService.getAllProductInstances();
        return new ResponseEntity<>(instances, HttpStatus.OK);
    }

    // Status'e göre ProductInstance listele
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ProductInstance>> getProductInstancesByStatus(@PathVariable ProductInstanceStatus status) {
        List<ProductInstance> instances = productInstanceService.getProductInstancesByStatus(status);
        return new ResponseEntity<>(instances, HttpStatus.OK);
    }

    // SerialNumber'a göre ProductInstance bul
    @GetMapping("/serial-number/{serialNumber}")
    public ResponseEntity<ProductInstance> getProductInstanceBySerialNumber(@PathVariable String serialNumber) {
        Optional<ProductInstance> productInstance = productInstanceService.getProductInstanceBySerialNumber(serialNumber);
        return productInstance.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Belirli bir ProductModel'e ait ProductInstance'ları getir
    @GetMapping("/product-model/{productModelId}")
    public ResponseEntity<List<ProductInstance>> getProductInstancesByProductModel(@PathVariable Long productModelId) {
        List<ProductInstance> instances = productInstanceService.getProductInstancesByProductModel(productModelId);
        return new ResponseEntity<>(instances, HttpStatus.OK);
    }

    // ProductInstance güncelle
    @PutMapping("/{id}")
    public ResponseEntity<ProductInstance> updateProductInstance(@PathVariable Long id,
                                                                 @RequestBody ProductInstance productInstance) {
        ProductInstance updatedInstance = productInstanceService.updateProductInstance(id, productInstance);
        return new ResponseEntity<>(updatedInstance, HttpStatus.OK);
    }

    // ProductInstance sil
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProductInstance(@PathVariable Long id) {
        productInstanceService.deleteProductInstance(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // ProductInstance durumunu güncelle
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProductInstance> updateStatus(@PathVariable Long id,
                                                        @RequestBody ProductInstanceStatus newStatus) {
        ProductInstance updatedInstance = productInstanceService.updateStatus(id, newStatus);
        return new ResponseEntity<>(updatedInstance, HttpStatus.OK);
    }
    // Toplu ekleme için
    @PostMapping("/bulk")
    public ResponseEntity<List<ProductInstance>> createProductInstances(
            @RequestBody ProductInstanceRequest request) {
        List<ProductInstance> createdInstances = productInstanceService.createProductInstances(
                request.getProductInstances(), request.getProductModelId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdInstances);
    }
    
}

