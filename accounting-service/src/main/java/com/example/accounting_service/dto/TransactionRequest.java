package com.example.accounting_service.dto;

import com.example.accounting_service.entity.Transaction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransactionRequest {

    @NotNull(message = "Tipe transaksi wajib diisi")
    private Transaction.TransactionType type;

    @NotNull(message = "Branch ID wajib diisi")
    private Long branchId;

    private Long relatedBranchId;

    @NotNull(message = "Product ID wajib diisi")
    private Long productId;

    @NotBlank(message = "Nama produk wajib diisi")
    private String productName;

    @NotNull(message = "Quantity wajib diisi")
    @Min(value = 1, message = "Quantity minimal 1")
    private Integer quantity;

    @NotNull(message = "Harga satuan wajib diisi")
    @Min(value = 0, message = "Harga tidak boleh negatif")
    private Double unitPrice;

    private String notes;

    public Transaction.TransactionType getType() { return type; }
    public void setType(Transaction.TransactionType type) { this.type = type; }
    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }
    public Long getRelatedBranchId() { return relatedBranchId; }
    public void setRelatedBranchId(Long relatedBranchId) { this.relatedBranchId = relatedBranchId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}