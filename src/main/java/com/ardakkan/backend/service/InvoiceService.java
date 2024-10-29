package com.ardakkan.backend.service;

import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.repo.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

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
}
