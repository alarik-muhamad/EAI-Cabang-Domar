package com.example.inventory_service.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.inventory_service.dto.TransferRequestDto;
import com.example.inventory_service.entity.StockMovement;
import com.example.inventory_service.entity.TransferRequest;
import com.example.inventory_service.event.TransferEvent;
import com.example.inventory_service.kafka.InventoryKafkaProducer;
import com.example.inventory_service.rabbitmq.RabbitMQProducer;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockRepository;
import com.example.inventory_service.repository.TransferRequestRepository;

@Service
public class TransferService {

    private final TransferRequestRepository transferRepository;
    private final StockRepository stockRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final InventoryKafkaProducer kafkaProducer;
    private final RabbitMQProducer rabbitMQProducer;

    public TransferService(TransferRequestRepository transferRepository,
                           StockRepository stockRepository,
                           ProductRepository productRepository,
                           StockService stockService,
                           InventoryKafkaProducer kafkaProducer,
                           RabbitMQProducer rabbitMQProducer) {
        this.transferRepository = transferRepository;
        this.stockRepository = stockRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
        this.kafkaProducer = kafkaProducer;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public List<TransferRequest> getPendingTransfers() {
        return transferRepository.findByStatus(TransferRequest.TransferStatus.PENDING);
    }

    @Transactional
    public TransferRequest requestTransfer(TransferRequestDto dto) {
        var product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        var fromStock = stockRepository
                .findByProductIdAndBranchId(dto.getProductId(), dto.getFromBranchId())
                .orElseThrow(() -> new RuntimeException("Stok tidak ditemukan di cabang asal"));

        if (fromStock.getQuantity() < dto.getQuantity())
            throw new RuntimeException("Stok cabang asal tidak cukup");

        TransferRequest transfer = new TransferRequest();
        transfer.setReferenceNumber("TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        transfer.setProduct(product);
        transfer.setFromBranchId(dto.getFromBranchId());
        transfer.setToBranchId(dto.getToBranchId());
        transfer.setQuantity(dto.getQuantity());
        transfer.setStatus(TransferRequest.TransferStatus.PENDING);
        transfer.setNotes(dto.getNotes());
        transfer.setCreatedAt(LocalDateTime.now());
        TransferRequest saved = transferRepository.save(transfer);

        kafkaProducer.sendTransferRequested(new TransferEvent(
                saved.getReferenceNumber(), product.getId(),
                dto.getFromBranchId(), dto.getToBranchId(),
                dto.getQuantity(), "PENDING"));

        return saved;
    }

    @Transactional
    public TransferRequest approveTransfer(String referenceNumber) {
        TransferRequest transfer = transferRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Transfer tidak ditemukan"));

        if (transfer.getStatus() != TransferRequest.TransferStatus.PENDING)
            throw new RuntimeException("Transfer sudah diproses");

        transfer.setStatus(TransferRequest.TransferStatus.APPROVED);
        transfer.setUpdatedAt(LocalDateTime.now());
        TransferRequest saved = transferRepository.save(transfer);

        TransferEvent event = new TransferEvent(
                saved.getReferenceNumber(), saved.getProduct().getId(),
                saved.getFromBranchId(), saved.getToBranchId(),
                saved.getQuantity(), "APPROVED");

        kafkaProducer.sendTransferApproved(event);
        // rabbitMQ dipindah ke executeApprovedTransfer setelah transfer benar-benar selesai

        return saved;
    }

    @Transactional
    public void executeApprovedTransfer(String referenceNumber) {
        TransferRequest transfer = transferRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Transfer tidak ditemukan"));

        com.example.inventory_service.dto.StockRequest outReq = new com.example.inventory_service.dto.StockRequest();
        outReq.setProductId(transfer.getProduct().getId());
        outReq.setBranchId(transfer.getFromBranchId());
        outReq.setQuantity(transfer.getQuantity());
        outReq.setMovementType(StockMovement.MovementType.TRANSFER_OUT);
        outReq.setReferenceNumber(transfer.getReferenceNumber());
        stockService.adjustStock(outReq);

        com.example.inventory_service.dto.StockRequest inReq = new com.example.inventory_service.dto.StockRequest();
        inReq.setProductId(transfer.getProduct().getId());
        inReq.setBranchId(transfer.getToBranchId());
        inReq.setQuantity(transfer.getQuantity());
        inReq.setMovementType(StockMovement.MovementType.TRANSFER_IN);
        inReq.setReferenceNumber(transfer.getReferenceNumber());
        stockService.adjustStock(inReq);

        transfer.setStatus(TransferRequest.TransferStatus.COMPLETED);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);

        // Kirim notif ke accounting setelah transfer benar-benar COMPLETED
        TransferEvent doneEvent = new TransferEvent(
                transfer.getReferenceNumber(), transfer.getProduct().getId(),
                transfer.getFromBranchId(), transfer.getToBranchId(),
                transfer.getQuantity(), "COMPLETED");
        rabbitMQProducer.sendTransferDoneNotif(doneEvent);
    }

    @Transactional
    public void rejectTransfer(String referenceNumber) {
        TransferRequest transfer = transferRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new RuntimeException("Transfer tidak ditemukan"));
        transfer.setStatus(TransferRequest.TransferStatus.REJECTED);
        transfer.setUpdatedAt(LocalDateTime.now());
        transferRepository.save(transfer);
    }
}