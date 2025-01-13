package com.ardakkan.backend.repo;

import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.entity.RefundRequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    // Belirli bir kullanıcıya ait tüm faturaları bulmak
    List<Invoice> findByUserId(Long userId);

    // Belirli bir siparişe ait faturayı bulmak
    Invoice findByOrderId(Long idOrder);

    @Query("SELECT i FROM Invoice i WHERE i.createdAt BETWEEN :startDate AND :endDate")
    List<Invoice> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

