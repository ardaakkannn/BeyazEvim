package com.ardakkan.backend.controller;

import org.jfree.chart.JFreeChart;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.ardakkan.backend.service.RevenueChartService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class RevenueReportController {

    private final RevenueChartService revenueChartService; // RevenueService, generateMonthlyRevenuePdf fonksiyonunu çağıracak

    public RevenueReportController(RevenueChartService revenueService) {
        this.revenueChartService = revenueService;
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<byte[]> getMonthlyRevenuePdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            // 1) LocalDate -> LocalDateTime dönüşümü
            LocalDateTime startDateTime = startDate.atStartOfDay();     // Örn: 2025-01-12T00:00:00
            LocalDateTime endDateTime   = endDate.atTime(23, 59, 59);   // Örn: 2025-01-12T23:59:59

            // 2) Servisten rapor verilerini çek
            Map<String, Map<String, Double>> monthlyData = 
                    revenueChartService.calculateMonthlyData(startDateTime, endDateTime);

            // 3) Grafiği oluştur
            JFreeChart chart = revenueChartService.createMonthlyRevenueChart(monthlyData);

            // 4) PDF’i oluştur
            byte[] pdfBytes = revenueChartService.generateMonthlyRevenuePdf(chart, monthlyData);

            // 5) Response olarak PDF döndür
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("inline", "MonthlyRevenue.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (IllegalArgumentException ex) {
            // İstenen tarih aralığında veri yoksa veya başka bir hata olursa
            return ResponseEntity.badRequest().body(ex.getMessage().getBytes());
        }
    }

}
