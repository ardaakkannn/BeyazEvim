package com.ardakkan.backend.service;


import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.dto.ProductModelDTO;
import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderItem;
import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.repo.ProductModelRepository;

import jakarta.transaction.Transactional;

import com.ardakkan.backend.repo.OrderItemRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductModelService {

    private final ProductModelRepository productModelRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final NotificationService NotificationService;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;

    @Autowired
    public ProductModelService(ProductModelRepository productModelRepository,
                               ProductInstanceRepository productInstanceRepository,NotificationService NotificationService,OrderItemRepository orderItemRepository,
                               OrderRepository orderRepository, OrderItemService orderItemService) {
        this.productModelRepository = productModelRepository;
        this.productInstanceRepository = productInstanceRepository;
		this.NotificationService = NotificationService;
		this.orderItemRepository= orderItemRepository;
		this.orderRepository= orderRepository;
		this.orderItemService=orderItemService;
		
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
            existingProductModel.setPopularity(productModelDetails.getPopularity());
            existingProductModel.setRating(productModelDetails.getRating());
            existingProductModel.setActive(productModelDetails.isActive());
            existingProductModel.setDiscount(productModelDetails.getDiscount());
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
    public  List<ProductModelDTO> getAllProductModelsDTO(){
    	 List<ProductModel> allProducts = productModelRepository.findAll();
    	 
    	 return allProducts.stream()
                 .map(this::convertToDTO)
                 .collect(Collectors.toList());
    	
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
    
    @Transactional
    public void addStock(Long productModelId, int quantityToAdd) {
        // Ürün modelini getir
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));

        for (int i = 0; i < quantityToAdd; i++) {
            // Yeni bir ProductInstance oluştur
            ProductInstance productInstance = new ProductInstance();
            productInstance.setProductModel(productModel);
            productInstance.setStatus(ProductInstanceStatus.IN_STOCK); // Varsayılan durum: IN_STOCK
            productInstance.setSerialNumber("temporary_serial_number");
            // Önce ProductInstance'ı kaydet ki ID oluşsun
            productInstance = productInstanceRepository.save(productInstance);

            // Seri numarasını ayarla: productModelId+productInstanceId
            String serialNumber = String.format("SN%d000%d", productModelId, productInstance.getId());
            productInstance.setSerialNumber(serialNumber);

            // Güncellenmiş ProductInstance'ı tekrar kaydet
            productInstanceRepository.save(productInstance);
        }

        // Güncel stok sayısını kontrol et
        int updatedStock = getAvailableProductInstanceCount(productModelId);

        // Eğer stok sıfırdan pozitif bir değere geçtiyse bildirim gönder
        if (updatedStock > 0 && updatedStock - quantityToAdd == 0) {
            NotificationService.notifyUsersForRestock(productModel);
        }
    }
    
    
    
    @Transactional
    public void decreaseStock(Long productModelId, int quantityToRemove) {
        // Ürün modelini getir
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new RuntimeException("ProductModel not found with id: " + productModelId));

        // Toplam mevcut ürün sayısını kontrol et
        int availableStock = getAvailableProductInstanceCount(productModelId);
        if (availableStock < quantityToRemove) {
            throw new IllegalStateException("Yeterli stok yok. Toplam mevcut stok: " + availableStock);
        }

        int remainingToRemove = quantityToRemove;

        // IN_STOCK durumundaki ProductInstance'ları getir
        List<ProductInstance> inStockInstances = productInstanceRepository
                .findTopNByProductModelIdAndStatus(productModelId, ProductInstanceStatus.IN_STOCK, PageRequest.of(0, quantityToRemove));

        for (ProductInstance instance : inStockInstances) {
            removeProductInstanceFromCarts(instance);
            productInstanceRepository.delete(instance);
            remainingToRemove--;
            if (remainingToRemove == 0) {
                break;
            }
        }

        // Eğer hala silinecek ürün kaldıysa IN_CART durumundakileri getir ve işle
        if (remainingToRemove > 0) {
            List<ProductInstance> inCartInstances = productInstanceRepository
                    .findTopNByProductModelIdAndStatus(productModelId, ProductInstanceStatus.IN_CART, PageRequest.of(0, remainingToRemove));

            for (ProductInstance instance : inCartInstances) {
                removeProductInstanceFromCarts(instance);
                productInstanceRepository.delete(instance);
                remainingToRemove--;
                if (remainingToRemove == 0) {
                    break;
                }
            }
        }
    }
    
    private void removeProductInstanceFromCarts(ProductInstance productInstance) {
        // Bu ProductInstance'ın bulunduğu tüm OrderItem'ları getir
        List<OrderItem> orderItems = orderItemRepository.findByProductInstanceId(productInstance.getId());

        for (OrderItem orderItem : orderItems) {
            // ProductInstance'ı listeden kaldır
            orderItem.getProductInstanceIds().remove(productInstance.getId());

            // Ürün birim fiyatını al
            Double productPrice = orderItem.getUnitPrice();

            // Miktarı güncelle
            orderItem.setQuantity(orderItem.getQuantity() - 1);

            // Eğer miktar sıfıra düştüyse:
            if (orderItem.getQuantity() <= 0) {
                // Order'ın toplam fiyatından OrderItem'ın toplam fiyatını çıkar
                Order order = orderItem.getOrder();
                order.setTotalPrice(order.getTotalPrice() - (productPrice * (orderItem.getQuantity() + 1))); // Toplam fiyatı azalt
                orderRepository.save(order);

                // OrderItem'ı sil
                orderItemRepository.delete(orderItem);
            } else {
                // Miktar > 0 ise, toplam fiyatı güncelle ve OrderItem'ı kaydet
                Order order = orderItem.getOrder();
                order.setTotalPrice(order.getTotalPrice() - productPrice);
                orderRepository.save(order);
                orderItemRepository.save(orderItem);
            }
        }
    }





    
    public List<ProductModelDTO> searchProductModels(String searchString) {
        // Arama terimi boşsa, tüm ürünleri döndür
        if (searchString == null || searchString.trim().isEmpty()) {
            return getAllProductModelsDTO();
        }

        // distributorInformation, description, name ve category alanlarında arama yap
        List<ProductModel> productModels = productModelRepository
                .findByDistributorInformationContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrNameContainingIgnoreCaseOrCategory_CategoryNameContainingIgnoreCase(
                        searchString, searchString, searchString, searchString);

        // Arama sonuçlarını DTO'ya dönüştür
        return productModels.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    
   // Toplu ProductModel ekleme
    public List<ProductModel> saveAllProductModels(List<ProductModel> productModels) {
        return productModelRepository.saveAll(productModels);
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
    
    public void organizeDiscount(Long productId, Double discountRate) {
        if (discountRate == null || discountRate < 0 || discountRate > 100) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 100.");
        }

        // Ürünü ID ile bul
        ProductModel productModel = productModelRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product with ID " + productId + " not found."));

        // İndirimi ayarla
        productModel.setDiscount(discountRate);
        updateProductPrice(productId,productModel.getDiscountedPrice());

        // Güncellenmiş ürünü kaydet
        productModelRepository.save(productModel);

        System.out.println("Discount of " + discountRate + "% applied to Product with ID: " + productId);
        NotificationService.notifyUsersAboutDiscount(productId);
        
    }
    
    @Transactional
    public void updateProductPrice(Long productModelId, Double newPrice) {
        // Ürünü bul ve fiyatını güncelle
        ProductModel productModel = productModelRepository.findById(productModelId)
                .orElseThrow(() -> new IllegalStateException("ProductModel bulunamadı: " + productModelId));
        productModel.setPrice(newPrice);
        productModelRepository.save(productModel);

        // Sadece IN_CART durumundaki siparişler için fiyatları güncelle
        orderItemService.updateOrderAndOrderItemPrices(productModelId, newPrice);
    }


    public ProductModelDTO convertToDTO(ProductModel productModel) {
        ProductModelDTO dto = new ProductModelDTO();
        dto.setId(productModel.getId());
        dto.setName(productModel.getName());
        dto.setDescription(productModel.getDescription());
        dto.setPrice(productModel.getPrice());
        dto.setBrand(productModel.getDistributorInformation());
        dto.setImage_path(productModel.getPhotoPath());
        dto.setPopulerity(productModel.getPopularity());
        dto.setRating(productModel.getRating());
        dto.setColor(productModel.getColor());
        dto.setWarranty(productModel.getWarranty());
        // Stok bilgisini al
        int stockCount = getAvailableProductInstanceCount(productModel.getId());
        dto.setStockCount(stockCount);
        // İndirim bilgilerini ata
        dto.setDiscount(productModel.getDiscount());
        dto.setDiscountedPrice(productModel.getDiscountedPrice());
        return dto;
    }


}

