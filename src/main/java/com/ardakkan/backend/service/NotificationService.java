package com.ardakkan.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.RefundRequest;
import com.ardakkan.backend.entity.User;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.repo.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserRepository userRepository; // Kullanıcıları getirmek için
    
    @Autowired
    private ProductModelRepository productModelRepository; // Ürünleri getirmek için

    
    public void notifyUsersAboutDiscount(Long productId) {
        // Ürünü veritabanından bul
        ProductModel product = productModelRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Ürünü wishlist'ine ekleyen kullanıcıları bul
        List<User> usersWithProductInWishlist = userRepository.findAllByWishlistContaining(product);

        // Kullanıcılara mail gönder
        for (User user : usersWithProductInWishlist) {
            String subject = "Great News! " + product.getName() + " is now on discount!";
            String text = "Dear " + user.getFirstName() + ",\n\n" +
                    "The product '" + product.getName() + "' is now on discount!\n" +
                    "Check it out now and don't miss this great opportunity.\n\n" +
                    "Best regards,\nYour Shopping Team";

            mailService.sendSimpleMail(user.getEmail(), subject, text);
        }
    }
    
    public void notifyUsersForRestock(ProductModel product) {
        List<User> usersWithWishlist = userRepository.findUsersByWishlistProduct(product.getId());

        for (User user : usersWithWishlist) {
            String subject = "Product Back in Stock!";
            String message = "Dear " + user.getFirstName() + ",\n\n"
                    + "The product \"" + product.getName() + "\" is now back in stock! Don't miss your chance to purchase it.\n\n"
                    + "Best regards,\nThe Beyaz Evim Team";

            mailService.sendSimpleMail(user.getEmail(), subject, message);
        }
    }
    
    public void notifyOrderCancellation(String userEmail, String firstName, Long orderId) {
        String subject = "Your Order #" + orderId + " Has Been Canceled";
        String text = "Dear " + firstName + ",\n\n" +
                      "Your order with ID #" + orderId + " has been successfully canceled.\n" +
                      "The refund process will be completed within 5-10 business days.\n\n" +
                      "Thank you for your understanding.\n\n" +
                      "Best regards,\n" +
                      "Beyaz Evim Team";

        mailService.sendSimpleMail(userEmail, subject, text);
    }
    
    public void notifyRefundRequestCreated(String userEmail, RefundRequest refundRequest) {
    	long orderId= refundRequest.getOrder().getId();
    	// ProductModel'i getir
    	ProductModel productModel = productModelRepository.findById(refundRequest.getOrderItem().getProductModelId())
    	        .orElseThrow(() -> new IllegalStateException("ProductModel not found for ID: " + refundRequest.getOrderItem().getProductModelId()));

    	// Ürün adını al
    	String productName = productModel.getName();

    	String subject = "Refund Request Created for Order #" + orderId;
        String text = String.format(
                "Dear Customer,\n\n" +
                "We have received your refund request for the product '%s' in Order #%d.\n" +
                "Our team is currently reviewing your request. You will be notified once a decision is made.\n\n" +
                "Thank you for your patience.\n\n" +
                "Best regards,\n" +
                "Beyaz Evim Team",
                productName, orderId
        );

        mailService.sendSimpleMail(userEmail, subject, text);
    }

    public void notifyRefundApproved(String userEmail, RefundRequest refundRequest) {
    	long orderId= refundRequest.getOrder().getId();
    	// ProductModel'i getir
    	ProductModel productModel = productModelRepository.findById(refundRequest.getOrderItem().getProductModelId())
    	        .orElseThrow(() -> new IllegalStateException("ProductModel not found for ID: " + refundRequest.getOrderItem().getProductModelId()));
    	// Ürün adını al
    	String productName = productModel.getName();
    	// Refund amount'u al
        double refundAmount = refundRequest.getOrderItem().getUnitPrice();
    	
    	String subject = "Refund Approved for Order #" + orderId;
        String message = String.format(
                "Dear Customer,\n\n" +
                "Your refund request for the product '%s' in Order #%d has been approved.\n" +
                "A total refund of $%.2f will be processed to your payment method shortly.\n\n" +
                "If you have any questions, please feel free to contact our support team.\n\n" +
                "Best regards,\n" +
                "Beyaz Evim Team",
                productName, orderId, refundAmount
        );

        mailService.sendSimpleMail(userEmail, subject, message);
    }

    public void notifyRefundRejected(String userEmail, RefundRequest refundRequest) {
    	long orderId= refundRequest.getOrder().getId();
    	// ProductModel'i getir
    	ProductModel productModel = productModelRepository.findById(refundRequest.getOrderItem().getProductModelId())
    	        .orElseThrow(() -> new IllegalStateException("ProductModel not found for ID: " + refundRequest.getOrderItem().getProductModelId()));
    	// Ürün adını al
    	String productName = productModel.getName();
    	
    	String subject = "Refund Rejected for Order #" + orderId;
        String text = String.format(
                "Dear Customer,\n\n" +
                "We regret to inform you that your refund request for the product '%s' in Order #%d has been rejected.\n" +
                "For further assistance, please contact our support team.\n\n" +
                "Thank you for understanding.\n\n" +
                "Best regards,\n" +
                "Beyaz Evim Team",
                productName, orderId
        );
        mailService.sendSimpleMail(userEmail, subject, text);
    }
    
    public void notifyRefundProcessed(String userEmail, Long orderId, Double refundAmount) {
        String subject = "Refund Processed for Order #" + orderId;
        String text = "Dear Customer,\n\n" +
                      "We have successfully processed your refund for Order #" + orderId + ".\n" +
                      "The total amount of $" + refundAmount + " has been refunded to your payment method.\n\n" +
                      "If you have any questions, please don't hesitate to contact our support team.\n\n" +
                      "Best regards,\n" +
                      "Beyaz Evim Team";

        mailService.sendSimpleMail(userEmail, subject, text);
    }

}
