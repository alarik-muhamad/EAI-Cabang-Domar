package com.example.inventory_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TransferRequestDto {

    @NotNull(message = "Product ID wajib diisi")
    private Long productId;

    @NotNull(message = "From branch wajib diisi")
    private Long fromBranchId;

    @NotNull(message = "To branch wajib diisi")
    private Long toBranchId;

    @NotNull(message = "Quantity wajib diisi")
    @Min(value = 1, message = "Quantity minimal 1")
    private Integer quantity;

    private String notes;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getFromBranchId() { return fromBranchId; }
    public void setFromBranchId(Long fromBranchId) { this.fromBranchId = fromBranchId; }
    public Long getToBranchId() { return toBranchId; }
    public void setToBranchId(Long toBranchId) { this.toBranchId = toBranchId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}