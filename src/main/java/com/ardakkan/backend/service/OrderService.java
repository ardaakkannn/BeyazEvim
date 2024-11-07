package com.ardakkan.backend.service;

import com.ardakkan.backend.dto.OrderDTO;
import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderStatus;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.repo.InvoiceRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

		private final OrderRepository orderRepository;
	    private final UserRepository userRepository;
	    private final InvoiceRepository invoiceRepository;

	    @Autowired
	    public OrderService(OrderRepository orderRepository, UserRepository userRepository, InvoiceRepository invoiceRepository) {
	        this.orderRepository = orderRepository;
	        this.userRepository = userRepository;
	        this.invoiceRepository = invoiceRepository;
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
            createInvoiceForOrder(existingOrder);  // Fatura oluşturma fonksiyonu çağır
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
        newCart.setOrderDate(new Date());
        newCart.setTotalPrice(0.0);

        // Yeni sepeti kaydediyoruz
        orderRepository.save(newCart);
    }

    // Fatura oluşturma işlemi
    private void createInvoiceForOrder(Order order) {
        Invoice invoice = new Invoice();
        invoice.setOrder(order);  // Fatura, bu siparişe bağlı olacak
        invoice.setUser(order.getUser());  // Faturayı siparişi veren kullanıcıya set ediyoruz
        invoice.setTotalPrice(order.getTotalPrice());  // Toplam tutar, siparişin toplamı olacak
        invoice.setCreatedAt(new Date());  // Faturanın oluşturulma tarihini şu anki tarihe set ediyoruz
        invoice.setDetails("Sipariş için fatura: " + order.getId());

        // Faturayı kaydet
        invoiceRepository.save(invoice);
    }

    // Siparişi silme (entity kullanıyor)
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new IllegalStateException("Sipariş bulunamadı: " + id);
        }
        orderRepository.deleteById(id);
    }

 // Entity'yi DTO'ya dönüştürme işlemi
    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setStatus(order.getStatus());  // Enum tipiyle status set ediliyor
        orderDTO.setTotalPrice(order.getTotalPrice());
        orderDTO.setUserId(order.getUser().getId());

        List<Long> orderItemIds = order.getOrderItems()
                .stream()
                .map(item -> item.getId())
                .collect(Collectors.toList());
        orderDTO.setOrderItemIds(orderItemIds);

        return orderDTO;
    }

}
