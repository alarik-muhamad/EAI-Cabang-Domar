package com.example.inventory_service.saga;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Menyimpan state saga transfer stok antar cabang.
 * Setiap step dicatat agar compensating transaction bisa dijalankan
 * jika ada kegagalan di tengah alur.
 */
@Entity
@Table(name = "transfer_saga_states")
public class TransferSagaState {

    public enum SagaStatus {
        STARTED,
        STOCK_RESERVED,       // stok cabang asal sudah dikurangi
        TRANSFER_APPROVED,    // pusat sudah approve
        STOCK_DELIVERED,      // stok cabang tujuan sudah ditambah
        COMPLETED,
        COMPENSATING,         // sedang rollback
        COMPENSATED           // rollback selesai
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String referenceNumber;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Long fromBranchId;

    @Column(nullable = false)
    private Long toBranchId;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SagaStatus status;

    @Column
    private String failureReason;

    /** Berapa kali retry sudah dilakukan untuk step saat ini */
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    public TransferSagaState() {}

    public static TransferSagaState create(String referenceNumber, Long productId,
                                            Long fromBranchId, Long toBranchId,
                                            Integer quantity) {
        TransferSagaState s = new TransferSagaState();
        s.referenceNumber = referenceNumber;
        s.productId = productId;
        s.fromBranchId = fromBranchId;
        s.toBranchId = toBranchId;
        s.quantity = quantity;
        s.status = SagaStatus.STARTED;
        s.createdAt = LocalDateTime.now();
        return s;
    }

    public void advance(SagaStatus nextStatus) {
        this.status = nextStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.failureReason = reason;
        this.status = SagaStatus.COMPENSATING;
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementRetry() {
        this.retryCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getReferenceNumber() { return referenceNumber; }
    public Long getProductId() { return productId; }
    public Long getFromBranchId() { return fromBranchId; }
    public Long getToBranchId() { return toBranchId; }
    public Integer getQuantity() { return quantity; }
    public SagaStatus getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public Integer getRetryCount() { return retryCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setStatus(SagaStatus status) { this.status = status; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
