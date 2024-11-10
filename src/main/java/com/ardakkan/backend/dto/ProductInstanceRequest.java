package com.ardakkan.backend.dto;
import java.util.List;

import com.ardakkan.backend.entity.ProductInstance;

public class ProductInstanceRequest {

	 private Long productModelId;
	 private List<ProductInstance> productInstances;
	public Long getProductModelId() {
		return productModelId;
	}
	public void setProductModelId(Long productModelId) {
		this.productModelId = productModelId;
	}
	public List<ProductInstance> getProductInstances() {
		return productInstances;
	}
	public void setProductInstances(List<ProductInstance> productInstances) {
		this.productInstances = productInstances;
	}
	
}
