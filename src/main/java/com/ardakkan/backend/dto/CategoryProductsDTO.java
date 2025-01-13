package com.ardakkan.backend.dto;
import java.util.List;

import com.ardakkan.backend.entity.ProductModel;

public class CategoryProductsDTO {
    private List<ProductModel> productModels;
    private List<String> brands;

    // Getter ve Setter metodları
    public List<ProductModel> getProductModels() {
        return productModels;
    }

    public void setProductModels(List<ProductModel> productModels) {
        this.productModels = productModels;
    }

    public List<String> getBrands() {
        return brands;
    }

    public void setBrands(List<String> brands) {
        this.brands = brands;
    }
}

