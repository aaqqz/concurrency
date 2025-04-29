package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.facade.LockSockFacade;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class StockDatabaseLockServiceTest {

    /**
     * Mysql 을 활용한 다양한 방법
     *
     *   - Pessimistic Lock (경합이 빈번하다면 추천하지 않음)
     * 실제로 데이터에 Lock 을 걸어서 정합성을 맞추는 방법입니다.
     * exclusive lock 을 걸게되며 다른 트랜잭션에서는 lock 이 해제되기전에 데이터를 가져갈 수 없게됩니다.
     * 데드락이 걸릴 수 있기때문에 주의하여 사용하여야 합니다.
     *
     *   - Optimistic Lock
     *   (경합이 빈번하다면 추천하지 않음, 재시도 하기 위해 오래 걸림)
     * 실제로 Lock 을 이용하지 않고 버전을 이용함으로써 정합성을 맞추는 방법입니다.
     * 먼저 데이터를 읽은 후에 update 를 수행할 때 현재 내가 읽은 버전이 맞는지 확인하며 업데이트 합니다.
     * 내가 읽은 버전에서 수정사항이 생겼을 경우에는 application에서 다시 읽은후에 작업을 수행해야 합니다.
     *
     *   - Named Lock
     * 이름을 가진 metadata locking 입니다.
     * 이름을 가진 lock 을 획득한 후 해제할때까지 다른 세션은 이 lock 을 획득할 수 없도록 합니다.
     * 주의할점으로는 transaction 이 종료될 때 lock 이 자동으로 해제되지 않습니다.
     * 별도의 명령어로 해제를 수행해주거나 선점시간이 끝나야 해제됩니다.
     */

    @Autowired
    private StockDatabaseLockService stockService;

    @Autowired
    private LockSockFacade optimisticLockSockFacade;

    @Autowired
    private StockRepository stockRepository;

    @Test
    void 동시에_100개의_재고_감소_pessimisticLock() throws InterruptedException {
        // given
        Stock savedStock = stockRepository.save(new Stock(1L, 100L));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease_pessimisticLock(savedStock.getId(), 1L);
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

    @Test
    void 동시에_100개의_재고_감소_optimisticLock() throws InterruptedException {
        // given
        Stock savedStock = stockRepository.save(new Stock(1L, 100L));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockSockFacade.decrease_optimisticLock(savedStock.getId(), 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
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

    @Test
    void 동시에_100개의_재고_감소_namedLock() throws InterruptedException {
        // given
        Stock savedStock = stockRepository.save(new Stock(1L, 100L));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockSockFacade.decrease_namedLock(savedStock.getId(), 1L);
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