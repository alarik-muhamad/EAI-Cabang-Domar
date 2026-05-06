package com.example.inventory_service.repository;

import com.example.inventory_service.entity.TransferRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransferRequestRepository extends JpaRepository<TransferRequest, Long> {
    Optional<TransferRequest> findByReferenceNumber(String referenceNumber);
    List<TransferRequest> findByFromBranchId(Long branchId);
    List<TransferRequest> findByToBranchId(Long branchId);
    List<TransferRequest> findByStatus(TransferRequest.TransferStatus status);
}