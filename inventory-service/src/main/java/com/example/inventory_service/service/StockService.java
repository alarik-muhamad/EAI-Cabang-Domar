package com.example.inventory_service.service;

import com.example.inventory_service.dto.StockRequest;
import com.example.inventory_service.entity.Stock;
import com.example.inventory_service.entity.StockMovement;
import com.example.inventory_service.event.LowStockEvent;
import com.example.inventory_service.event.StockUpdatedEvent;
import com.example.inventory_service.kafka.InventoryKafkaProducer;
import com.example.inventory_service.rabbitmq.RabbitMQProducer;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockMovementRepository;
import com.example.inventory_service.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMovementRepository movementRepository;
    private final ProductRepository productRepository;
    private final InventoryKafkaProducer kafkaProducer;
    private final RabbitMQProducer rabbitMQProducer;

    public StockService(StockRepository stockRepository,
                        StockMovementRepository movementRepository,
                        ProductRepository productRepository,
                        InventoryKafkaProducer kafkaProducer,
                        RabbitMQProducer rabbitMQProducer) {
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
        this.kafkaProducer = kafkaProducer;
        this.rabbitMQProducer = rabbitMQProducer;
    }

    public List<Stock> getStockByBranch(Long branchId) {
        return stockRepository.findByBranchId(branchId);
    }

    public List<Stock> getLowStocks() {
        return stockRepository.findLowStocks();
    }

    public List<Stock> getLowStocksByBranch(Long branchId) {
        return stockRepository.findLowStocksByBranch(branchId);
    }

    @Transactional
    public Stock adjustStock(StockRequest request) {
        var product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Produk tidak ditemukan"));

        Stock stock = stockRepository
                .findByProductIdAndBranchId(request.getProductId(), request.getBranchId())
                .orElseGet(() -> {
                    Stock s = new Stock();
                    s.setProduct(product);
                    s.setBranchId(request.getBranchId());
                    s.setQuantity(0);
                    s.setMinimumStock(10);
                    return s;
                });

        switch (request.getMovementType()) {
            case STOCK_IN, TRANSFER_IN -> stock.setQuantity(stock.getQuantity() + request.getQuantity());
            case STOCK_OUT, TRANSFER_OUT -> {
                if (stock.getQuantity() < request.getQuantity())
                    throw new RuntimeException("Stok tidak cukup");
                stock.setQuantity(stock.getQuantity() - request.getQuantity());
            }
            case ADJUSTMENT -> stock.setQuantity(request.getQuantity());
        }

        stock.setLastUpdated(LocalDateTime.now());
        Stock saved = stockRepository.save(stock);

        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setBranchId(request.getBranchId());
        movement.setMovementType(request.getMovementType());
        movement.setQuantity(request.getQuantity());
        movement.setReferenceNumber(request.getReferenceNumber());
        movement.setNotes(request.getNotes());
        movement.setCreatedAt(LocalDateTime.now());
        movementRepository.save(movement);

        kafkaProducer.sendStockUpdated(new StockUpdatedEvent(
                product.getId(), request.getBranchId(),
                saved.getQuantity(), request.getMovementType().name(),
                request.getReferenceNumber()));

        if (saved.getQuantity() <= saved.getMinimumStock()) {
            LowStockEvent lowStock = new LowStockEvent(
                    product.getId(), product.getName(),
                    request.getBranchId(), saved.getQuantity(), saved.getMinimumStock());
            kafkaProducer.sendLowStockAlert(lowStock);
            rabbitMQProducer.sendRestockNotif(lowStock);
        }

        return saved;
    }
}