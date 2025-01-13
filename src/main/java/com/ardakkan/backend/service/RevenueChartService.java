package com.ardakkan.backend.service;
import com.ardakkan.backend.entity.ProductModel;
import com.ardakkan.backend.entity.RefundRequest;
import com.ardakkan.backend.entity.RefundStatus;
import com.ardakkan.backend.entity.Invoice;
import com.ardakkan.backend.entity.Order;
import com.ardakkan.backend.entity.OrderStatus;
import com.ardakkan.backend.entity.ProductInstance;
import com.ardakkan.backend.repo.InvoiceRepository;
import com.ardakkan.backend.repo.OrderRepository;
import com.ardakkan.backend.repo.ProductInstanceRepository;
import com.ardakkan.backend.repo.ProductModelRepository;
import com.ardakkan.backend.repo.RefundRequestRepository;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import org.jfree.chart.JFreeChart;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Table;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;




@Service
public class RevenueChartService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RefundRequestRepository refundRequestRepository;

    public Map<String, Map<String, Double>> calculateMonthlyData(LocalDateTime startDate, LocalDateTime endDate) {
        // 1) İlgili tarih aralığındaki Order'ları ve RefundRequest'leri çek
        List<Order> orders = orderRepository.findOrdersByDateRange(startDate, endDate);
        List<RefundRequest> refundRequests = refundRequestRepository.findByDateRange(startDate, endDate);

        // 2) Eğer hiç veri yoksa exception fırlat
        if (orders.isEmpty() && refundRequests.isEmpty()) {
            throw new IllegalArgumentException("No data found for the given date range.");
        }

        // 3) Aylık gelir (Revenue) hesapla
        Map<String, Double> monthlyRevenue = orders.stream()
                .filter(order ->
                    order.getStatus() == OrderStatus.PURCHASED ||
                    order.getStatus() == OrderStatus.DELIVERED ||
                    order.getStatus() == OrderStatus.SHIPPED
                )
                .collect(Collectors.groupingBy(
                    // LocalDateTime -> Ay bilgisini alıp stringe çeviriyoruz
                    order -> order.getOrderDate().getMonth().toString(),
                    Collectors.summingDouble(Order::getTotalPrice)
                ));

        // 4) Aylık iptal (Cancelled) toplamlarını hesapla
        Map<String, Double> monthlyCancelled = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CANCELED)
                .collect(Collectors.groupingBy(
                    order -> order.getOrderDate().getMonth().toString(),
                    Collectors.summingDouble(Order::getTotalPrice)
                ));

        // 5) Aylık iade (Refund) toplamlarını hesapla
        Map<String, Double> monthlyRefunds = refundRequests.stream()
                .filter(refund -> refund.getStatus() == RefundStatus.APPROVED)
                .collect(Collectors.groupingBy(
                    // RefundRequest'te de LocalDateTime olduğunu varsayarak
                    refund -> refund.getRequestedAt().getMonth().toString(),
                    Collectors.summingDouble(refund -> refund.getOrderItem().getUnitPrice())
                ));

        // 6) Sonuç haritasını hazırlarken, tüm ayları tek bir set altında toplayalım
        Set<String> allMonths = new HashSet<>();
        allMonths.addAll(monthlyRevenue.keySet());
        allMonths.addAll(monthlyCancelled.keySet());
        allMonths.addAll(monthlyRefunds.keySet());

        // 7) Her ay için gelir, iptal ve iade değerlerini ekle
        Map<String, Map<String, Double>> result = new HashMap<>();
        for (String month : allMonths) {
            Map<String, Double> data = new HashMap<>();
            data.put("Revenue",   monthlyRevenue.getOrDefault(month, 0.0));
            data.put("Cancelled", monthlyCancelled.getOrDefault(month, 0.0));
            data.put("Refund",    monthlyRefunds.getOrDefault(month, 0.0));
            result.put(month, data);
        }

        return result;
    }
    public JFreeChart createMonthlyRevenueChart(Map<String, Map<String, Double>> monthlyData) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Map<String, Double>> entry : monthlyData.entrySet()) {
            String month = entry.getKey();
            Map<String, Double> data = entry.getValue();

            dataset.addValue(data.get("Revenue"), "Revenue", month);
            dataset.addValue(-data.get("Cancelled"), "Cancelled", month); // Negatif bar
            dataset.addValue(-data.get("Refund"), "Refund", month);       // Negatif bar
        }

        return ChartFactory.createBarChart(
                "Monthly Revenue and Losses", // Title
                "Month", // X-axis Label
                "Amount ($)", // Y-axis Label
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }

    public byte[] generateMonthlyRevenuePdf(JFreeChart chart, Map<String, Map<String, Double>> monthlyData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // PDF Writer oluştur
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Başlık ekle
            document.add(new Paragraph("Monthly Revenue Report")
                    .setBold()
                    .setFontSize(18)
                    .setMarginBottom(20));

            // Grafiği PDF'ye ekle
            BufferedImage bufferedImage = chart.createBufferedImage(500, 300);
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", imageStream);
            ImageData imageData = ImageDataFactory.create(imageStream.toByteArray());
            Image chartImage = new Image(imageData);
            document.add(chartImage);

            // Tabloyu ekle
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1}))
                    .useAllAvailableWidth();
            table.addHeaderCell("Month");
            table.addHeaderCell("Revenue ($)");
            table.addHeaderCell("Cancelled ($)");
            table.addHeaderCell("Refund ($)");

            // Verileri tabloya ekle
            for (Map.Entry<String, Map<String, Double>> entry : monthlyData.entrySet()) {
                String month = entry.getKey();
                Map<String, Double> data = entry.getValue();
                table.addCell(month);
                table.addCell(String.format("%.2f", data.getOrDefault("Revenue", 0.0)));
                table.addCell(String.format("%.2f", data.getOrDefault("Cancelled", 0.0)));
                table.addCell(String.format("%.2f", data.getOrDefault("Refund", 0.0)));
            }

            document.add(table);

            // Belgeyi kapat ve byte array olarak döndür
            document.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate monthly revenue PDF", e);
        }
    }

   

}

