package com.ardakkan.backend.service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.ardakkan.backend.entity.OrderItem;
import com.ardakkan.backend.entity.OrderStatus;
import com.ardakkan.backend.entity.ProductInstanceStatus;
import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.repo.OrderItemRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.entity.ProductInstance;
import jakarta.transaction.Transactional;

import com.ardakkan.backend.dto.OrderItemDTO;
import com.ardakkan.backend.dto.ProductModelDTO;
import com.ardakkan.backend.entity.ProductModel;

@Service
@Transactional
public class OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final ProductModelRepository productModelRepository;
    private final ProductModelService productModelService;

    @Autowired
    public OrderItemService(OrderItemRepository orderItemRepository, 
                            OrderRepository orderRepository, 
                            ProductInstanceRepository productInstanceRepository,
                            ProductModelRepository productModelRepository, @Lazy ProductModelService productModelService) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productInstanceRepository = productInstanceRepository;
        this.productModelRepository = productModelRepository;
		this.productModelService = productModelService;
    }
    
    @Transactional
    public OrderItemDTO addProductToCart(Long orderId, Long productModelId) {
        // Order'ı getir
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order bulunamadı: " + orderId));

        // IN_STOCK veya IN_CART durumunda olan ProductInstance'ları getir
        List<ProductInstance> productInstances = productInstanceRepository
                .findAllByProductModelIdAndStatusIn(productModelId, List.of(ProductInstanceStatus.IN_STOCK, ProductInstanceStatus.IN_CART));

        if (productInstances.isEmpty()) {
            throw new IllegalStateException("Stokta veya sepette uygun ürün bulunamadı: " + productModelId);
        }

        // Order ve ProductModel üzerinden mevcut bir OrderItem kontrolü
        Optional<OrderItem> existingOrderItemOpt = orderItemRepository
                .findByOrderAndProductModelId(order, productModelId);

        OrderItem orderItem;

        if (existingOrderItemOpt.isPresent()) {
            orderItem = existingOrderItemOpt.get();

            // Daha önce eklenmemiş bir ProductInstance bul
            ProductInstance productInstance = findAvailableProductInstance(productInstances, orderItem);

            // Eğer uygun ProductInstance yoksa hata fırlat
            if (productInstance == null) {
                throw new IllegalStateException("Tüm uygun ProductInstance'lar zaten eklenmiş: " + productModelId);
            }

            // Yeni ProductInstance'ı OrderItem'e ekle
            addProductInstanceToOrderItem(orderItem, productInstance, order);

        } else {
            // Yeni OrderItem oluştur ve ilk uygun ProductInstance'ı ekle
            ProductInstance productInstance = productInstances.stream().findFirst().orElseThrow();
            orderItem = createNewOrderItem(order, productModelId, productInstance);
        }

        // OrderItem'ı kaydet ve DTO'ya dönüştür
        orderItemRepository.save(orderItem);
        return convertToOrderItemDTO(orderItem);
    }

    private ProductInstance findAvailableProductInstance(List<ProductInstance> productInstances, OrderItem orderItem) {
        for (ProductInstance instance : productInstances) {
            if (!orderItem.getProductInstanceIds().contains(instance.getId())) {
                return instance; // Daha önce eklenmemiş olanı döner
            }
        }
        return null;
    }

    private void addProductInstanceToOrderItem(OrderItem orderItem, ProductInstance productInstance, Order order) {
        Long productInstanceId = productInstance.getId();
        Double productPrice = productInstance.getProductModel().getDiscountedPrice();

        orderItem.setQuantity(orderItem.getQuantity() + 1);
        orderItem.getProductInstanceIds().add(productInstanceId);
        order.setTotalPrice(order.getTotalPrice() + productPrice);

        // ProductInstance durumunu IN_CART yap
        productInstance.setStatus(ProductInstanceStatus.IN_CART);
        productInstanceRepository.save(productInstance);
    }

    private OrderItem createNewOrderItem(Order order, Long productModelId, ProductInstance productInstance) {
        Long productInstanceId = productInstance.getId();
        Double productPrice = productInstance.getProductModel().getDiscountedPrice();

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductModelId(productModelId);
        orderItem.setUnitPrice(productPrice);
        orderItem.setQuantity(1);
        orderItem.getProductInstanceIds().add(productInstanceId);
        order.setTotalPrice(order.getTotalPrice() + productPrice);

        // ProductInstance durumunu IN_CART yap
        productInstance.setStatus(ProductInstanceStatus.IN_CART);
        productInstanceRepository.save(productInstance);

        return orderItem;
    }


    
    
    
    @Transactional
    public OrderItemDTO removeProductFromCart(Long orderId, Long productModelId) {
        // Order'ı getir
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order bulunamadı: " + orderId));

        // OrderItem'ı bul
        OrderItem orderItem = orderItemRepository
                .findByOrderAndProductModelId(order, productModelId)
                .orElseThrow(() -> new IllegalStateException("Ürün sepette bulunamadı: " + productModelId));

        // Ürünün birim fiyatını al
        Double productPrice = orderItem.getUnitPrice();

        if (orderItem.getQuantity() > 1) {
            // Miktar > 1 ise, miktarı azalt ve ID'yi listeden çıkar
            orderItem.setQuantity(orderItem.getQuantity() - 1);

            // Listeden bir ProductInstance ID'si çıkar
            Long productInstanceId = orderItem.getProductInstanceIds().remove(0);

            // ProductInstance durumunu güncelle
            ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                    .orElseThrow(() -> new IllegalStateException("ProductInstance bulunamadı: " + productInstanceId));
            productInstance.setStatus(ProductInstanceStatus.IN_STOCK);
            productInstanceRepository.save(productInstance);

            // Order'ın toplam fiyatını güncelle
            order.setTotalPrice(order.getTotalPrice() - productPrice);
            orderRepository.save(order);
            orderItemRepository.save(orderItem);
        } else {
            // Miktar 1 ise, OrderItem'ı tamamen kaldır
            orderItem.getProductInstanceIds().forEach(productInstanceId -> {
                ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                        .orElseThrow(() -> new IllegalStateException("ProductInstance bulunamadı: " + productInstanceId));
                productInstance.setStatus(ProductInstanceStatus.IN_STOCK);
                productInstanceRepository.save(productInstance);
            });

            // Order'ın toplam fiyatından OrderItem'ın toplam fiyatını çıkar
            order.setTotalPrice(order.getTotalPrice() - (productPrice * orderItem.getQuantity()));
            orderRepository.save(order);

            // OrderItem'ı tamamen sil
            orderItemRepository.delete(orderItem);
        }

        return  convertToOrderItemDTO(orderItem);
    }

    
    public ProductModelDTO getProductModelByOrderItemId(Long orderItemId) {
        // OrderItem'ı bul
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalStateException("OrderItem bulunamadı: " + orderItemId));
        
        // OrderItem içindeki ilk ProductInstance ID'sini al
        Long productInstanceId = orderItem.getProductInstanceIds().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OrderItem için ProductInstance ID bulunamadı: " + orderItemId));
        
        // ProductInstance'ı ID üzerinden bul
        ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                .orElseThrow(() -> new IllegalStateException("ProductInstance bulunamadı: " + productInstanceId));
        
        // ProductInstance içindeki ProductModel'i bul
        ProductModel productModel = productModelRepository.findById(productInstance.getProductModel().getId())
                .orElseThrow(() -> new IllegalStateException("ProductModel bulunamadı: " + productInstance.getProductModel().getId()));
        
        // ProductModel'den DTO'ya dönüşüm yap
        return convertToDTO(productModel);
    }


    
    public OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        orderItemDTO.setOrderItemId(orderItem.getId());
        orderItemDTO.setQuantity(orderItem.getQuantity());
        orderItemDTO.setUnitPrice(orderItem.getUnitPrice());

        // İlk ProductInstance ID'sini al
        Long productInstanceId = orderItem.getProductInstanceIds().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("OrderItem içinde ProductInstance ID bulunamadı: " + orderItem.getId()));

        // ProductInstance'ı ID ile bul
        ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                .orElseThrow(() -> new IllegalStateException("ProductInstance bulunamadı: " + productInstanceId));

        // ProductModel'i ProductInstance üzerinden al
        ProductModel productModel = productInstance.getProductModel();
        ProductModelDTO productModelDTO = convertToDTO(productModel);

        // DTO'ya ProductModel bilgilerini ekle
        orderItemDTO.setProductModel(productModelDTO);

        return orderItemDTO;
    }
    
    
    @Transactional
    public void updateOrderAndOrderItemPrices(Long productModelId, Double newPrice) {
        // Sadece IN_CART durumundaki siparişlere ait OrderItem'ları getir
        List<OrderItem> orderItems = orderItemRepository.findByProductModelIdAndInCartOrders(productModelId);

        for (OrderItem orderItem : orderItems) {
            Order order = orderItem.getOrder();
            Double oldPrice = orderItem.getUnitPrice();

            // Fiyat farkını hesapla ve OrderItem fiyatını güncelle
            Double priceDifference = newPrice - oldPrice;
            orderItem.setUnitPrice(newPrice);

            // Order toplam fiyatını güncelle
            Double totalPrice = newPrice * orderItem.getQuantity();
            order.setTotalPrice(totalPrice);

            // Değişiklikleri kaydet
            orderItemRepository.save(orderItem);
            orderRepository.save(order);
        }
    }


    
    // ProductModel -> ProductModelDTO dönüşümü
    private ProductModelDTO convertToDTO(ProductModel productModel) {
    	return productModelService.convertToDTO(productModel);
    }
    
    


}

