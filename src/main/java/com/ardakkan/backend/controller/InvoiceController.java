package com.ardakkan.backend.controller;

import com.ardakkan.backend.dto.InvoiceDTO;
import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.List;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    // Yeni bir fatura oluşturma
    @PostMapping
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice) {
        Invoice savedInvoice = invoiceService.createInvoice(invoice);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInvoice);
    }

    // ID'ye göre fatura bulma
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDTO> findInvoiceById(@PathVariable Long id) {
        InvoiceDTO invoice = invoiceService.findInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    // Tüm faturaları listeleme
    @GetMapping
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        List<InvoiceDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    // Belirli bir kullanıcıya göre faturaları bulma
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<InvoiceDTO>> findInvoiceByUserId(@PathVariable Long userId) {
        List<InvoiceDTO> invoices = invoiceService.findInvoiceByUserId(userId);
        return ResponseEntity.ok(invoices);
    }

    // Fatura güncelleme
    @PutMapping("/{id}")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable Long id,
            @RequestBody Invoice updatedInvoice) {
        Invoice updated = invoiceService.updateInvoice(id, updatedInvoice);
        return ResponseEntity.ok(updated);
    }

    // Faturayı silme
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/order/{orderId}/pdf")
    public ResponseEntity<byte[]> getInvoicePdfByOrderId(@PathVariable Long orderId) {
        byte[] pdfData = invoiceService.generateInvoicePdfByOrderId(orderId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_order_" + orderId + ".pdf")
                .body(pdfData);
    }
    
 // Belirli bir LocalDate aralığında faturaları listeleme
    @GetMapping("/invoices-by-date")
    public ResponseEntity<List<InvoiceDTO>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // LocalDate -> LocalDateTime dönüşümü
            LocalDateTime startDateTime = startDate.atStartOfDay();     // Örn: 2024-01-01T00:00:00
            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);    // Örn: 2024-12-31T23:59:59

            // Faturaları servisten al
            List<InvoiceDTO> invoices = invoiceService.getInvoicesByDateRange(startDateTime, endDateTime);

            // Yanıtı döndür
            return ResponseEntity.ok(invoices);
        } catch (IllegalArgumentException ex) {
            // Hatalı parametrelerde bad request döndür
            return ResponseEntity.badRequest().body(null);
        }
    }

}