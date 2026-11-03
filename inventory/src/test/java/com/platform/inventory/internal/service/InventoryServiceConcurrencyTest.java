package com.platform.inventory.internal.service;


import com.platform.common.exceptions.InventoryExceptions.InsufficientInventoryException;
import com.platform.inventory.InventoryTestApplication;
import com.platform.inventory.internal.entity.Inventory;
import com.platform.inventory.internal.repository.InventoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(classes = InventoryTestApplication.class)
public class InventoryServiceConcurrencyTest {
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InventoryRepository inventoryRepository;
    private static final String TEST_SKU = "CONCURRENT-SKU-100";

    @BeforeEach
    void setUp(){
        inventoryRepository.save(new Inventory(TEST_SKU,10,0));
    }

    @AfterEach
    void tearDown(){
        inventoryRepository.deleteById(TEST_SKU);
    }

    @Test
    @DisplayName("20 concurrent threads attempting to reserve 10 units must reserve exactly 10 and reject 10")
    void testConcurrentStockReservation() throws InterruptedException{
        int threadCount = 20;
        ExecutorService service = Executors.newFixedThreadPool(threadCount);

        // Ensures all 20 threads are created and ready before firing
        CountDownLatch readyLatch = new CountDownLatch(threadCount);

        // Fires all 20 threads at the exact same instant
        CountDownLatch startLatch = new CountDownLatch(1);
        // Waits for all 20 threads to complete execution
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientStockCount = new AtomicInteger(0);
        AtomicInteger unexpectedErrorCount = new AtomicInteger(0);

        for(int i=0;i<threadCount;i++){
            service.submit(() -> {
                readyLatch.countDown();
                try{
                    startLatch.await();
                    inventoryService.reserveStock(TEST_SKU,1);
                    successCount.incrementAndGet();
                }catch (InsufficientInventoryException e){
                    insufficientStockCount.incrementAndGet();
                }catch (Exception e){
                    unexpectedErrorCount.incrementAndGet();
                }
                finally {
                    doneLatch.countDown();
                }
            });
        }

        // 1. Wait until all 20 threads are waiting at startLatch
        readyLatch.await(5, TimeUnit.SECONDS);

        // 2. All threads hit inventoryService.reserveStock at once
        startLatch.countDown();
        // 3. wait till all the threads are done
        boolean completed = doneLatch.await(30,TimeUnit.SECONDS);

        assertThat(completed).isTrue();
        service.shutdown();

        //Thread outcomes
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(insufficientStockCount.get()).isEqualTo(10);
        assertThat(unexpectedErrorCount.get()).isEqualTo(0);
    }

}
