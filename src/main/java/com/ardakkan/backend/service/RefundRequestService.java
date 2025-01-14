package com.ardakkan.backend.service;
import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.dto.OrderDTO;
import com.ardakkan.backend.dto.OrderItemDTO;
import com.ardakkan.backend.dto.ProductModelDTO;
import com.ardakkan.backend.dto.RefundRequestDto;
import com.ardakkan.backend.entity.*;
import com.ardakkan.backend.repo.InvoiceRepository;
import com.ardakkan.backend.repo.OrderItemRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;
import com.ardakkan.backend.repo.RefundRequestRepository;
import com.ardakkan.backend.repo.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RefundRequestService {

    private final RefundRequestRepository refundRequestRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public RefundRequestService(RefundRequestRepository refundRequestRepository,
                                OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                ProductInstanceRepository productInstanceRepository,
                                UserRepository userRepository, NotificationService notificationService) {
        this.refundRequestRepository = refundRequestRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productInstanceRepository = productInstanceRepository;
        this.userRepository = userRepository;
        this.notificationService= notificationService;
    }
    
    private RefundRequestDto convertToDto(RefundRequest entity) {
        RefundRequestDto dto = new RefundRequestDto();
        dto.setId(entity.getId());
        dto.setOrderId(entity.getOrder() != null ? entity.getOrder().getId() : null);
        dto.setOrderItemId(entity.getOrderItem() != null ? entity.getOrderItem().getId() : null);
        dto.setProductModelId(entity.getProductModelId());
        dto.setProductInstanceId(entity.getProductInstanceId());
        dto.setStatus(entity.getStatus());
        dto.setRequestedAt(entity.getRequestedAt());
        dto.setApprovedOrRejectedAt(entity.getApprovedOrRejectedAt());
        if (entity.getApprovedBy() != null) {
            dto.setApprovedByUserId(entity.getApprovedBy().getId());
        }
        return dto;
    }


    public void createRefundRequest(Long orderId, Long productModelId) {
        // Siparişi getir
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
        
        
        LocalDateTime orderDate = order.getOrderDate();
        long daysSinceOrder = ChronoUnit.DAYS.between(orderDate, LocalDateTime.now());
        if (daysSinceOrder > 30) {
            throw new IllegalStateException("30 gün geçtiği için iade talebi yapılamaz.");
        }
        // Siparişin durumunu kontrol et
        if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("Only delivered orders can be refunded.");
        }

        // OrderItem'ı bul
        OrderItem orderItem = orderItemRepository.findByOrderAndProductModelId(order, productModelId)
                .orElseThrow(() -> new IllegalStateException("OrderItem with productModelId not found in this order: " + productModelId));

        // Refund edilecek ilk ProductInstance'ı seç
        if (orderItem.getProductInstanceIds().isEmpty()) {
            throw new IllegalStateException("No products available to refund in this OrderItem.");
        }

        Long productInstanceId = orderItem.getProductInstanceIds().get(0);

        // RefundRequest oluştur
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrder(order);
        refundRequest.setOrderItem(orderItem);
        refundRequest.setProductModelId(productModelId);
        refundRequest.setProductInstanceId(productInstanceId);
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setRequestedAt(LocalDateTime.now());

        refundRequestRepository.save(refundRequest);
        
        notificationService.notifyRefundRequestCreated(order.getUser().getEmail(), refundRequest);
    }
    
    
    public void createRefundRequestForOrder(Long orderId) {
        // 1) Siparişi getir
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        // 1.1) Siparişin orderDate'ini kontrol et (30 günden fazla geçmişse iade talebi reddedilir)
        // Burada order.getOrderDate() veya order.getCreatedAt() gibi bir alanı kullanmalısınız (örneğin createdAt).
        // Örnek olarak "getCreatedAt()" olduğunu varsayıyoruz.
        LocalDateTime orderDate = order.getOrderDate();
        long daysSinceOrder = ChronoUnit.DAYS.between(orderDate, LocalDateTime.now());
        if (daysSinceOrder > 30) {
            throw new IllegalStateException("30 gün geçtiği için iade talebi yapılamaz.");
        }

        // 2) Sipariş durumunu kontrol et (Sadece DELIVERED siparişlerde iade başvurusuna izin ver)
        if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("Only delivered orders can be refunded.");
        }

        // 3) Tek ürünlü sipariş olduğunu doğrula
        List<OrderItem> orderItems = order.getOrderItems();
        if (orderItems == null || orderItems.size() != 1) {
            throw new IllegalStateException("This method is only for orders that contain exactly one item.");
        }

        // 4) OrderItem'ı al
        OrderItem singleItem = orderItems.get(0);

        // 5) Tek ürünlü siparişte dahi, ilgili Item'da herhangi bir ProductInstance yoksa iade edilemez
        if (singleItem.getProductInstanceIds().isEmpty()) {
            throw new IllegalStateException("No products available to refund in this OrderItem.");
        }

        // 6) productModelId bilgisi: OrderItem üzerinde tutuyorsanız, oradan alıyoruz
        Long productModelId = singleItem.getProductModelId();
        if (productModelId == null) {
            throw new IllegalStateException("No productModelId associated with this OrderItem.");
        }

        // 7) RefundRequest oluştur
        Long productInstanceId = singleItem.getProductInstanceIds().get(0);

        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setOrder(order);
        refundRequest.setOrderItem(singleItem);
        refundRequest.setProductModelId(productModelId);
        refundRequest.setProductInstanceId(productInstanceId);
        refundRequest.setStatus(RefundStatus.PENDING);
        refundRequest.setRequestedAt(LocalDateTime.now());

        refundRequestRepository.save(refundRequest);

        
     // 7.1) Order durumunu RETURN_REQUESTED olarak güncelle
        order.setStatus(OrderStatus.RETURN_REQUESTED);
        orderRepository.save(order);
        // 8) Bildirim gönder (opsiyonel)
        notificationService.notifyRefundRequestCreated(order.getUser().getEmail(), refundRequest);
    }


    public void approveRefundRequest(Long refundRequestId, boolean approved, UserDetails userDetails) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalStateException("RefundRequest not found: " + refundRequestId));

        refundRequest.setStatus(approved ? RefundStatus.APPROVED : RefundStatus.REJECTED);
        refundRequest.setApprovedOrRejectedAt(LocalDateTime.now());
        refundRequest.setApprovedBy(userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Approver not found.")));

        Order order = refundRequest.getOrder();
        refundRequestRepository.save(refundRequest);

        if (approved) {
            processRefund(refundRequest);
            notificationService.notifyRefundApproved(refundRequest.getOrder().getUser().getEmail(), refundRequest);
            //Sipariş durumunu RETURNED (ya da FULLY_REFUNDED vb.) olarak güncellemek istiyorsanız:
            order.setStatus(OrderStatus.RETURNED);
            orderRepository.save(order);
        }
        else {
        	// İade reddedildi: sipariş durumunu RETURN_REJECTED olarak güncelleyebilirsiniz
            order.setStatus(OrderStatus.RETURN_REJECTED);
            orderRepository.save(order);
        	notificationService.notifyRefundRejected(refundRequest.getOrder().getUser().getEmail(), refundRequest);
        }
    }

    private void processRefund(RefundRequest refundRequest) {
        // ProductInstance'ı stoğa geri ekle
        ProductInstance productInstance = productInstanceRepository.findById(refundRequest.getProductInstanceId())
                .orElseThrow(() -> new IllegalStateException("ProductInstance not found: " + refundRequest.getProductInstanceId()));

        productInstance.setStatus(ProductInstanceStatus.IN_STOCK);
        productInstanceRepository.save(productInstance);

        // OrderItem'dan ürün çıkar
        OrderItem orderItem = refundRequest.getOrderItem();
        orderItem.getProductInstanceIds().remove(refundRequest.getProductInstanceId());
        // İade edilen ürün ID'sini returned listesine ekle
        orderItem.getReturnedProductInstanceIds().add(refundRequest.getProductInstanceId());
        orderItem.setQuantity(orderItem.getQuantity() - 1);

        orderItemRepository.save(orderItem);
        

        // Siparişin toplam fiyatını güncelle
        Order order = refundRequest.getOrder();
        order.setTotalPrice(order.getTotalPrice() - orderItem.getUnitPrice());
        orderRepository.save(order);
    }
    
    @Transactional(readOnly = true)
    public List<RefundRequest> getAllRefundRequests(Optional<RefundStatus> status) {
        if (status.isPresent()) {
            // Duruma göre filtrelenmiş RefundRequest'leri getir
            return refundRequestRepository.findByStatus(status.get());
        } else {
            // Tüm RefundRequest'leri getir
            return refundRequestRepository.findAll();
        }
    }
    
    @Transactional(readOnly = true)
    public List<RefundRequest> getRefundRequestsByUserId(Long userId) {
        return refundRequestRepository.findAllByOrderUserId(userId);
    }

}

