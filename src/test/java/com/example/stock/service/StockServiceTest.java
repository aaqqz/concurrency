package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
//
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

    @Test
    void 동시에_100개의_재고_감소() throws InterruptedException {
        // given
        Stock savedStock = stockRepository.save(new Stock(1L, 100L));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(savedStock.getId(), 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Stock stock = stockRepository.findById(savedStock.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다."));
        assertThat(stock.getQuantity()).isNotZero();
    }

    @Test
    void 동시에_100개의_재고_감소_synchronized() throws InterruptedException {
        /*  - 거의 사용 안함
            ###### synchronized - 문제점 ###### (@Transactional 과 함께 사용 불가, 단일 프로세스에서 만 동작)

            1, @Transactional 의 동작 방식 때문에, 주석해야 함
            - @Transactional 은 Spring AOP 를 기반으로 동작하며, 프록시 객체를 생성하여 트랜잭션을 관리함
                프록시 객체는 기본적으로 메서드 호출을 가로채서 트랜잭션을 처리하기 때문에,
                synchronized 키워드가 메서드 레벨에서 제대로 동작하지 않을 수 있음
                따라서, synchronized 를 사용하려면 프록시가 아닌 실제 객체에서 동작하도록 해야 한다

            2, @Transaction 으로 생성된, proxy 객체에서는 synchronized 가 동작하지 않음
            - Spring의 프록시 객체는 기본적으로 메서드 호출을 가로채기 때문에, synchronized 키워드가 기대한 대로 동작하지 않을 수 있음
                특히, synchronized는 JVM 수준에서 스레드 동기화를 보장하지만, 프록시 객체는 호출을 위임하기 때문에 동기화가 제대로 적용되지 않을 수 있음


            3, 한대의 서버(process)에서만 동작하기 때문에, synchronized 를 사용하면, 멀티 서버 환경에서는 문제 발생
            - synchronized는 JVM 내부에서만 동작하며, 단일 프로세스 내의 스레드 간 동기화만 보장됨
                멀티 서버 환경에서는 각 서버의 JVM이 독립적으로 동작하므로, synchronized를 사용하면 동기화가 서버 간에 적용되지 않음
         */
        // given
        Stock savedStock = stockRepository.save(new Stock(1L, 100L));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease_synchronized(savedStock.getId(), 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        Stock stock = stockRepository.findById(savedStock.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재고입니다."));
        assertThat(stock.getQuantity()).isZero();
    }
}