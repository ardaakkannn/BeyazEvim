package com.ardakkan.backend.controller;

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
    public ResponseEntity<Invoice> findInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.findInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    // Tüm faturaları listeleme
    @GetMapping
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    // Belirli bir kullanıcıya göre faturaları bulma
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Invoice>> findInvoiceByUserId(@PathVariable Long userId) {
        List<Invoice> invoices = invoiceService.findInvoiceByUserId(userId);
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
    
    @GetMapping("/by-date-range")
    public ResponseEntity<List<Invoice>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<Invoice> invoices = invoiceService.getInvoicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(invoices);
    }

}