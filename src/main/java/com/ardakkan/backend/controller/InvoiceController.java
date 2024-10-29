package com.ardakkan.backend.controller;

import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
