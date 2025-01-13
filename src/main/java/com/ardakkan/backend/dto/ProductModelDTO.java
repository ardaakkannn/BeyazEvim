package com.ardakkan.backend.dto;

public class ProductModelDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Double discountedPrice;
    private Double discount=0.0;
    private String brand;
    private String image_path;
    private int stockCount; 
    private Double populerity;
    private Double rating;
    private String color;
    private String warranty;
    
    // Getter ve Setter'lar
    public Long getId() {
        return id;
    }

    public int getStockCount() {
		return stockCount;
	}

	public void setStockCount(int stockCount) {
		this.stockCount = stockCount;
	}

	public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getImage_path() {
		return image_path;
	}

	public void setImage_path(String image_path) {
		this.image_path = image_path;
	}

	public Double getPopulerity() {
		return populerity;
	}

	public void setPopulerity(Double populerity) {
		this.populerity = populerity;
	}

	public Double getRating() {
		return rating;
	}

	public void setRating(Double rating) {
		this.rating = rating;
	}

	public Double getDiscountedPrice() {
		return discountedPrice;
	}

	public void setDiscountedPrice(Double discountedprice) {
		this.discountedPrice = discountedprice;
	}

	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getWarranty() {
		return warranty;
	}

	public void setWarranty(String warranty) {
		this.warranty = warranty;
	}
	
    
}

