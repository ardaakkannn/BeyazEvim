package com.ardakkan.backend.repo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.RefundRequest;
import com.ardakkan.backend.entity.RefundStatus;

public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
	List<RefundRequest> findByStatus(RefundStatus status);

	 @Query("SELECT r FROM RefundRequest r WHERE r.requestedAt BETWEEN :startDate AND :endDate")
	    List<RefundRequest> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
	}

