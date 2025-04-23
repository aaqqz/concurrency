package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소
        // 갱신된 값을 저장

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다."));

        stock.decrease(quantity);
    }

//    @Transactional
    public synchronized void decrease_synchronized(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소
        // 갱신된 값을 저장

        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다."));

        stock.decrease(quantity);

        stockRepository.save(stock);
    }
}
