package com.example.stock.facade;

import com.example.stock.repository.StockRepository;
import com.example.stock.service.StockDatabaseLockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LockSockFacade {

    private final StockDatabaseLockService stockDatabaseLockService;
    private final StockRepository stockRepository;

    public void decrease_optimisticLock(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                stockDatabaseLockService.decrease_optimisticLock(id, quantity);

                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }
    }

    @Transactional
    public void decrease_namedLock(Long id, Long quantity) {
        try {
            stockRepository.getLock(id.toString());
            stockDatabaseLockService.decrease_namedLock(id, quantity);
        } finally {
            stockRepository.releaseLock(id.toString());
        }
    }
}
