package com.example.inventory_service.saga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransferSagaStateRepository extends JpaRepository<TransferSagaState, Long> {

    Optional<TransferSagaState> findByReferenceNumber(String referenceNumber);

    List<TransferSagaState> findByStatus(TransferSagaState.SagaStatus status);

    /** Dipakai scheduler untuk retry saga yang stuck di COMPENSATING */
    List<TransferSagaState> findByStatusAndRetryCountLessThan(
            TransferSagaState.SagaStatus status, int maxRetry);
}
