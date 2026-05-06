package com.example.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequest {

    @NotBlank(message = "SKU wajib diisi")
    private String sku;

    @NotBlank(message = "Nama produk wajib diisi")
    private String name;

    @NotBlank(message = "Kategori wajib diisi")
    private String category;

    @NotNull(message = "Harga wajib diisi")
    @Min(value = 1, message = "Harga minimal 1")
    private Double price;

    private String unit;

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}