package com.example.inventory_service.dto;

import com.example.inventory_service.entity.StockMovement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StockRequest {

    @NotNull(message = "Product ID wajib diisi")
    private Long productId;

    @NotNull(message = "Branch ID wajib diisi")
    private Long branchId;

    @NotNull(message = "Quantity wajib diisi")
    @Min(value = 1, message = "Quantity minimal 1")
    private Integer quantity;

    @NotNull(message = "Tipe movement wajib diisi")
    private StockMovement.MovementType movementType;

    private String referenceNumber;
    private String notes;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public StockMovement.MovementType getMovementType() { return movementType; }
    public void setMovementType(StockMovement.MovementType movementType) { this.movementType = movementType; }
    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}