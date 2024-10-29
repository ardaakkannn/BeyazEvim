package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Belirli bir kullanıcıya ait tüm faturaları bulmak
    List<Invoice> findByUserId(Long userId);

    // Belirli bir siparişe ait faturayı bulmak
    Invoice findByOrderId(Long idOrder);

}
