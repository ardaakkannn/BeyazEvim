package com.ardakkan.backend.service;

import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.repo.InvoiceRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    

    @Autowired
    private ProductModelRepository productModelRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductInstanceRepository productInstanceRepository;
    

    @Autowired
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // Fatura oluşturma
    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    // ID'ye göre fatura bulma
    public Invoice findInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Fatura bulunamadı: " + id));
    }

    // Tüm faturaları listeleme
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
    
   // Belirli bir LocalDateTime aralığında faturaları listeleme
    public List<Invoice> getInvoicesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Başlangıç ve bitiş tarihleri boş olamaz.");
        }
        
        return invoiceRepository.findByDateRange(startDate, endDate);
    }


    // Kullanıcıya göre faturaları bulma
    public List<Invoice> findInvoiceByUserId(Long userId) {
        return invoiceRepository.findByUserId(userId);
    }

    // Fatura güncelleme
    public Invoice updateInvoice(Long id, Invoice updatedInvoice) {
        Invoice existingInvoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Fatura bulunamadı: " + id));

        existingInvoice.setTotalPrice(updatedInvoice.getTotalPrice());
        existingInvoice.setDetails(updatedInvoice.getDetails());
        existingInvoice.setCreatedAt(updatedInvoice.getCreatedAt());

        return invoiceRepository.save(existingInvoice);
    }

    // Faturayı silme
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new IllegalStateException("Fatura bulunamadı: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalStateException("Fatura bulunamadı: " + invoiceId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Başlık ve genel bilgiler
            document.add(new Paragraph("Invoice")
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(20));
            document.add(new Paragraph("Invoice ID: " + invoice.getId()));
            document.add(new Paragraph("User: " + invoice.getUser().getFirstName() + " " + invoice.getUser().getLastName()));
            document.add(new Paragraph("Date: " + invoice.getCreatedAt()));
            document.add(new Paragraph("Total Price: $" + invoice.getTotalPrice()));

            // Tablo: Satın alınan ürünler
            document.add(new Paragraph("Purchased Products:").setBold().setMarginTop(20));
            Table purchasedTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1}))
                    .useAllAvailableWidth();
            purchasedTable.addHeaderCell("Product Name");
            purchasedTable.addHeaderCell("Quantity");
            purchasedTable.addHeaderCell("Price");

            invoice.getOrder().getOrderItems().forEach(orderItem -> {
                ProductModel productModel = productModelRepository.findById(orderItem.getProductModelId())
                        .orElseThrow(() -> new IllegalStateException("ProductModel not found for ID: " + orderItem.getProductModelId()));
                purchasedTable.addCell(productModel.getName());
                purchasedTable.addCell(String.valueOf(orderItem.getQuantity()));
                purchasedTable.addCell(String.valueOf(orderItem.getUnitPrice()));
            });

            document.add(purchasedTable);

            // Tablo: İade edilen ürünler
            boolean hasReturnedProducts = invoice.getOrder().getOrderItems().stream()
                    .anyMatch(orderItem -> !orderItem.getReturnedProductInstanceIds().isEmpty());

            if (hasReturnedProducts) {
                document.add(new Paragraph("Returned Products:").setBold().setMarginTop(20));
                Table returnedTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1}))
                        .useAllAvailableWidth();
                returnedTable.addHeaderCell("Product Name");
                returnedTable.addHeaderCell("Quantity");
                returnedTable.addHeaderCell("Price");

                invoice.getOrder().getOrderItems().forEach(orderItem -> {
                    for (Long returnedProductInstanceId : orderItem.getReturnedProductInstanceIds()) {
                        ProductInstance returnedProductInstance = productInstanceRepository.findById(returnedProductInstanceId)
                                .orElseThrow(() -> new IllegalStateException("ProductInstance not found for ID: " + returnedProductInstanceId));
                        ProductModel productModel = returnedProductInstance.getProductModel();

                        returnedTable.addCell(productModel.getName());
                        returnedTable.addCell("1"); // Her iade edilen ürün için miktar 1
                        returnedTable.addCell(String.valueOf(orderItem.getUnitPrice()));
                    }
                });

                document.add(returnedTable);
            }

            // Belgeyi kapat ve byte array olarak döndür
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF", e);
        }
    }


    
    public byte[] generateInvoicePdfByOrderId(Long orderId) {
        // Order ID ile faturayı bul
        Invoice invoice = invoiceRepository.findById(
                orderRepository.findById(orderId)
                        .orElseThrow(() -> new IllegalStateException("Order bulunamadı: " + orderId))
                        .getInvoice().getId()
        ).orElseThrow(() -> new IllegalStateException("Order'a bağlı fatura bulunamadı: " + orderId));

        // Fatura PDF'sini oluştur ve geri döndür
        return generateInvoicePdf(invoice.getId());
    }


}