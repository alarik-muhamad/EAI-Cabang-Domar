package com.example.inventory_service;

import com.example.inventory_service.entity.Product;
import com.example.inventory_service.entity.Stock;
import com.example.inventory_service.repository.ProductRepository;
import com.example.inventory_service.repository.StockRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;

    public DataInitializer(ProductRepository productRepository,
                           StockRepository stockRepository) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
    }

    @Override
    public void run(String... args) {
        if (productRepository.count() > 0) return;

        Product indomie = new Product();
        indomie.setSku("SKU-001");
        indomie.setName("Indomie Goreng");
        indomie.setCategory("Makanan");
        indomie.setPrice(3500.0);
        indomie.setUnit("pcs");
        productRepository.save(indomie);

        Product aqua = new Product();
        aqua.setSku("SKU-002");
        aqua.setName("Aqua 600ml");
        aqua.setCategory("Minuman");
        aqua.setPrice(4000.0);
        aqua.setUnit("botol");
        productRepository.save(aqua);

        Stock stock1 = new Stock();
        stock1.setProduct(indomie);
        stock1.setBranchId(1L);
        stock1.setQuantity(100);
        stock1.setMinimumStock(20);
        stock1.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock1);

        Stock stock2 = new Stock();
        stock2.setProduct(aqua);
        stock2.setBranchId(1L);
        stock2.setQuantity(50);
        stock2.setMinimumStock(15);
        stock2.setLastUpdated(LocalDateTime.now());
        stockRepository.save(stock2);

        System.out.println("Data inventory awal berhasil dibuat!");
    }
}