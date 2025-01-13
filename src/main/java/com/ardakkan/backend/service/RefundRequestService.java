package com.ardakkan.backend.service;
import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.dto.OrderDTO;
import com.ardakkan.backend.dto.OrderItemDTO;
import com.ardakkan.backend.dto.ProductModelDTO;
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

    public void createRefundRequest(Long orderId, Long productModelId) {
        // Siparişi getir
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

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

    public void approveRefundRequest(Long refundRequestId, boolean approved, UserDetails userDetails) {
        RefundRequest refundRequest = refundRequestRepository.findById(refundRequestId)
                .orElseThrow(() -> new IllegalStateException("RefundRequest not found: " + refundRequestId));

        refundRequest.setStatus(approved ? RefundStatus.APPROVED : RefundStatus.REJECTED);
        refundRequest.setApprovedOrRejectedAt(LocalDateTime.now());
        refundRequest.setApprovedBy(userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Approver not found.")));

        refundRequestRepository.save(refundRequest);

        if (approved) {
            processRefund(refundRequest);
            notificationService.notifyRefundApproved(refundRequest.getOrder().getUser().getEmail(), refundRequest);
        }
        else {
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

}

