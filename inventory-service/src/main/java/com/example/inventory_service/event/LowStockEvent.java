package com.example.inventory_service.event;

public class LowStockEvent {
    private Long productId;
    private String productName;
    private Long branchId;
    private Integer currentQuantity;
    private Integer minimumStock;

    public LowStockEvent() {}

    public LowStockEvent(Long productId, String productName, Long branchId,
                          Integer currentQuantity, Integer minimumStock) {
        this.productId = productId;
        this.productName = productName;
        this.branchId = branchId;
        this.currentQuantity = currentQuantity;
        this.minimumStock = minimumStock;
    }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Integer getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(Integer currentQuantity) { this.currentQuantity = currentQuantity; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }
}