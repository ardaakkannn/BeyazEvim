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
import com.ardakkan.backend.repo.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductInstanceRepository productInstanceRepository;
    private final ProductModelService productModelService;
    private final InvoiceService invoiceService;
    private final MailService MailService;
    private final NotificationService notificationService;
    
    //private final Invoice invoice;

    @Autowired
    public OrderService(OrderRepository orderRepository, UserRepository userRepository, 
    		InvoiceRepository invoiceRepository, ProductInstanceRepository productInstanceRepository, 
    		@Lazy ProductModelService productModelService, InvoiceService invoiceService,
    		MailService MailService, NotificationService notificationService,
    		OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.productInstanceRepository = productInstanceRepository;
        this.productModelService = productModelService;
        this.invoiceService = invoiceService;
        this.MailService=MailService;
        this.notificationService=notificationService;
        this.orderItemRepository=orderItemRepository;
    }

    // Sipariş oluşturma
    public Order createOrder(Order order) {
        Optional<User> user = userRepository.findById(order.getUser().getId());
        if (user.isPresent()) {
            order.setUser(user.get());
        } else {
            throw new IllegalStateException("Kullanıcı bulunamadı: " + order.getUser().getId());
        }

        return orderRepository.save(order);
    }

    // ID ile siparişi bulma - DTO döndürür
    public OrderDTO findOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Sipariş bulunamadı: " + id));

        return convertToDTO(order);  // DTO'ya dönüştürüyoruz
    }

    // Tüm siparişleri listeleme - DTO döndürür
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)  // Her siparişi DTO'ya dönüştürüyoruz
                .collect(Collectors.toList());
    }


    // Kullanıcının tüm siparişlerini getirme - DTO döndürür
    public List<OrderDTO> getOrdersByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDTO)  // Her siparişi DTO'ya dönüştürüyoruz
                .collect(Collectors.toList());
    }




    // Sipariş güncelleme
    public Order updateOrder(Long id, Order updatedOrder) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Sipariş bulunamadı: " + id));

        // Gerekli alanları güncelle
        existingOrder.setStatus(updatedOrder.getStatus());
        existingOrder.setTotalPrice(updatedOrder.getTotalPrice());
        existingOrder.setOrderItems(updatedOrder.getOrderItems());

        // Sipariş "Satın Alındı" statüsüne geçtiyse fatura oluştur
        if (updatedOrder.getStatus().equals(OrderStatus.PURCHASED)) {
            //createInvoiceForOrder(existingOrder);// Fatura oluşturma fonksiyonu çağır
            purchaseCartItems(id);
            createNewCart(existingOrder.getUser().getId());  // Satın alındığında yeni sepet oluştur
        }

        // Güncellenen siparişi kaydet
        return orderRepository.save(existingOrder);
    }


    // Yeni bir sepet (CART) oluşturma
    public void createNewCart(Long userId) {
        // Kullanıcının yeni bir sepet oluşturması
        Order newCart = new Order();
        newCart.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı: " + userId)));
        newCart.setStatus(OrderStatus.CART);  // Status "CART" olarak set ediliyor
        newCart.setOrderDate(LocalDateTime.now());
        newCart.setTotalPrice(0.0);

        // Yeni sepeti kaydediyoruz
        orderRepository.save(newCart);
    }

    // Sepetteki itemlerin alınması durumunda status'u "SOLD" a çevirme
    public ResponseEntity<byte[]> purchaseCartItems(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        // Check if the order status is "CART" to ensure it’s a valid cart
        if (!order.getStatus().equals(OrderStatus.CART)) {
            throw new IllegalStateException("Order is not in cart status: " + orderId);
        }

        // Sipariş durumunu PURCHASED olarak güncelle
        order.setStatus(OrderStatus.PURCHASED);
        orderRepository.save(order);
        // Process each order item
        for (OrderItem orderItem : order.getOrderItems()) {
            List<Long> productInstanceIds = orderItem.getProductInstanceIds();
            for (Long productInstanceId : productInstanceIds) {
                // Get product instance by ID
                ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                        .orElseThrow(() -> new IllegalStateException("ProductInstance not found: " + productInstanceId));

                // Update status to SOLD
                productInstance.setStatus(ProductInstanceStatus.SOLD);
                productInstanceRepository.save(productInstance);
            }
        }
        createInvoiceForOrder(order);
        // Generate the invoice PDF
        Long invoiceId = order.getInvoice().getId();
        byte[] pdfData = invoiceService.generateInvoicePdf(invoiceId);

        // Yeni bir CART oluştur
        createNewCart(order.getUser().getId());

        
        try {
            String userEmail = order.getUser().getEmail();
            String subject = "Your Invoice for Order #" + order.getId();
            String text = "Dear " + order.getUser().getFirstName() + ",\n\nThank you for your purchase! Please find your invoice attached.\n\nBest regards,\nBeyaz Evim";
            String attachmentName = "invoice_" + invoiceId + ".pdf";

            MailService.sendMailWithAttachment(userEmail, subject, text, attachmentName, pdfData);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email with invoice: " + e.getMessage());
        }


        // Return the PDF as a response
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + invoiceId + ".pdf")
                .body(pdfData);
    }

    // Fatura oluşturma işlemi
    private void createInvoiceForOrder(Order order) {
        Invoice invoice = new Invoice();
        invoice.setOrder(order);  // Fatura, bu siparişe bağlı olacak
        invoice.setUser(order.getUser());  // Faturayı siparişi veren kullanıcıya set ediyoruz
        invoice.setTotalPrice(order.getTotalPrice());  // Toplam tutar, siparişin toplamı olacak
        invoice.setCreatedAt(LocalDateTime.now());  // Faturanın oluşturulma tarihini şu anki tarihe set ediyoruz
        invoice.setDetails("Sipariş için fatura: " + order.getId());
        System.out.println("Invoice: " + invoice);

        // Faturayı kaydet
        invoiceRepository.save(invoice);
        order.setInvoice(invoice);
        orderRepository.save(order);

    }

    // Siparişi silme (entity kullanıyor)
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalStateException("Sipariş bulunamadı: " + id);
        }
        orderRepository.deleteById(id);
    }

    //Kulanıcının sepetini getirme 
    /*
    public List<OrderItemDTO> getUserCart(Long userId) {
        // Kullanıcının CART durumundaki siparişini bul
        Order cartOrder = orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)
                .orElseThrow(() -> new IllegalStateException("Sepet bulunamadı veya boş: " + userId));

        // Order'daki OrderItem'ları OrderItemDTO'ya dönüştür ve liste olarak döndür
        return cartOrder.getOrderItems().stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
    }
    */

    public OrderDTO getUserCart(Long userId) {
        // Kullanıcının CART durumundaki siparişini bul
        Order cartOrder = orderRepository.findByUserIdAndStatus(userId, OrderStatus.CART)
                .orElseThrow(() -> new IllegalStateException("Sepet bulunamadı:" + userId));

        // Order nesnesini OrderDTO'ya dönüştürerek döndür
        return convertToDTO(cartOrder);
    }
    
    public void cancelOrder(Long orderId) {
        // Siparişi bul
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        // Siparişin durumu "PURCHASED" mı kontrol et
        if (!order.getStatus().equals(OrderStatus.PURCHASED)) {
            throw new IllegalStateException("Only purchased orders can be canceled: " + orderId);
        }

        // Sipariş içindeki her ürünün durumunu "IN_STOCK" olarak güncelle
        for (OrderItem orderItem : order.getOrderItems()) {
            List<Long> productInstanceIds = orderItem.getProductInstanceIds();
            for (Long productInstanceId : productInstanceIds) {
                ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                        .orElseThrow(() -> new IllegalStateException("ProductInstance not found: " + productInstanceId));

                productInstance.setStatus(ProductInstanceStatus.IN_STOCK);
                productInstanceRepository.save(productInstance);
            }
        }

        // Sipariş durumunu "CANCELED" olarak güncelle
        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);

     // Kullanıcıya mail gönder (NotificationService üzerinden)
        notificationService.notifyOrderCancellation(
                order.getUser().getEmail(),
                order.getUser().getFirstName(),
                order.getId()
        );
    }
    
    
    
    
    @Transactional
    public void refundOrderItemProduct(Long orderId, Long productModelId) {
        // Siparişi al
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        // Sadece "DELIVERED" durumundaki siparişler iade edilebilir
        if (!order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new IllegalStateException("Only delivered orders can be refunded.");
        }

        // Belirtilen ProductModelId'ye sahip OrderItem'ı bul
        OrderItem orderItem = orderItemRepository.findByOrderAndProductModelId(order, productModelId)
                .orElseThrow(() -> new IllegalStateException("OrderItem with productModelId not found in this order: " + productModelId));

        // Satın alınan ProductInstance'lardan iade edilecek bir tanesini seç
        if (orderItem.getProductInstanceIds().isEmpty()) {
            throw new IllegalStateException("No products available to refund in this OrderItem.");
        }

        // İlk ProductInstance'ı purchased listeden çıkar
        Long productInstanceId = orderItem.getProductInstanceIds().remove(0);

        // ProductInstance'ı IN_STOCK durumuna güncelle
        ProductInstance productInstance = productInstanceRepository.findById(productInstanceId)
                .orElseThrow(() -> new IllegalStateException("ProductInstance not found: " + productInstanceId));
        productInstance.setStatus(ProductInstanceStatus.IN_STOCK);
        productInstanceRepository.save(productInstance);

        // İade edilen ürün ID'sini returned listesine ekle
        orderItem.getReturnedProductInstanceIds().add(productInstanceId);

        // OrderItem miktarını azalt
        orderItem.setQuantity(orderItem.getQuantity() - 1);

        // Siparişin toplam fiyatını güncelle
        double refundAmount = orderItem.getUnitPrice();
        order.setTotalPrice(order.getTotalPrice() - refundAmount);

        // Güncellemeleri kaydet
        orderItemRepository.save(orderItem);
        orderRepository.save(order);

        // Kullanıcıya bildirim gönder
        notificationService.notifyRefundProcessed(order.getUser().getEmail(), order.getId(), refundAmount);
    }


    
    
    
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // Siparişi veritabanından bul
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

        // Eğer yeni durum, geçerli bir geçiş değilse hata fırlat
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new IllegalStateException("Canceled orders cannot be updated: " + orderId);
        }

        // Eski durum ile yeni durumu kontrol et (örneğin RETURNED > SHIPPED yapılamaz)
        if (!isValidStatusTransition(order.getStatus(), newStatus)) {
            throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }

        // Siparişin durumunu güncelle
        order.setStatus(newStatus);
        orderRepository.save(order);

        return order; // Güncellenmiş siparişi geri döner
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Durum geçişi için kuralları belirle
        switch (currentStatus) {
            case CART:
                return newStatus == OrderStatus.PURCHASED;
            case PURCHASED:
                return newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.CANCELED;
            case SHIPPED:
                return newStatus == OrderStatus.DELIVERED;
            case DELIVERED:
                return newStatus == OrderStatus.RETURNED;
            default:
                return false;
        }
    }





    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setStatus(order.getStatus());
        orderDTO.setTotalPrice(order.getTotalPrice());
        orderDTO.setUserId(order.getUser().getId());

        List<OrderItemDTO> orderItemDTOs = order.getOrderItems()
                .stream()
                .map(this::convertToOrderItemDTO)
                .collect(Collectors.toList());
        orderDTO.setOrderItems(orderItemDTOs);

        return orderDTO;
    }

    private OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
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
        ProductModelDTO productModelDTO = convertToProductModelDTO(productModel);

        // DTO'ya ProductModel bilgilerini ekle
        orderItemDTO.setProductModel(productModelDTO);
        orderItemDTO.setReturned_quantity(orderItem.getReturnedProductInstanceIds().size());

        return orderItemDTO;
    }


    // ProductModel -> ProductModelDTO dönüşümü
    private ProductModelDTO convertToProductModelDTO(ProductModel productModel) {
        return productModelService.convertToDTO(productModel);
    }




}