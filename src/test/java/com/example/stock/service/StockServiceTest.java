package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

//    @BeforeEach
//    void setUp() {
//        stockRepository.save(new Stock(1L, 100L));
//    }

//    @AfterEach
//    void tearDown() {
//        stockRepository.deleteAll();
//    }

    @Test
    void 재고_감소() {
        // given
        Stock savedStock = stockRepository.save(new Stock(1L, 100L));

        // when
        stockService.decrease(savedStock.getId(), 1L);

        // then
        Stock stock = stockRepository.findById(savedStock.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다."));
        assertThat(stock.getQuantity()).isEqualTo(99L);
    }
}