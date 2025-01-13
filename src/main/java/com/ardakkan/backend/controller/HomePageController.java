package com.ardakkan.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ardakkan.backend.service.ProductModelService;
import com.ardakkan.backend.dto.ProductModelDTO;

@RestController
@RequestMapping("/api") // Tüm endpointlerin "/api" ile başlaması için
public class HomePageController {

	private final ProductModelService productModelService;

    @Autowired
    public HomePageController(ProductModelService productModelService) {
        this.productModelService = productModelService;
    }

    @GetMapping("/homepage")
    public List<ProductModelDTO> getHomePageProducts() {
        return productModelService.getAllProductModelsDTO(); // Rastgele 16 ürünü döndür
    }
}

