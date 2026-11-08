package com.platform.inventory.internal.service.serviceImpl;

import com.platform.common.exceptions.InventoryExceptions.InventoryNotFoundException;
import com.platform.common.exceptions.InventoryExceptions.LockAcquisitionException;
import com.platform.inventory.internal.entity.Inventory;
import com.platform.inventory.internal.repository.InventoryRepository;
import com.platform.inventory.internal.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    private static final String LOCK_PREFIX = "lock:inventory";
    private static final long WAIT_TIME_SECONDS = 15;
    private static final long LEASE_TIME_SECONDS = 5;

    @Override
    public void reserveStock(String sku,int quantity) {
        String lockKey = LOCK_PREFIX + sku;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try{
            isLocked = lock.tryLock(WAIT_TIME_SECONDS,LEASE_TIME_SECONDS, TimeUnit.SECONDS);
            if(!isLocked){
                log.warn("Unable to get lock for SKU {} within {}s",sku,WAIT_TIME_SECONDS);
                throw new LockAcquisitionException("System is busy handling requests for SKU"+ sku +"Please try again");
            }
            log.debug("Acquired lock for sku {} ",sku);
            transactionTemplate.executeWithoutResult(status -> {
                Inventory inventory = inventoryRepository.findById(sku).orElseThrow(() -> new InventoryNotFoundException("Couldn't find the requested item " + sku));
                inventory.reserve(quantity);
                inventoryRepository.save(inventory);
            });
            log.info("Successfully reserved {} units for SKU {}",quantity,sku);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(isLocked && lock.isHeldByCurrentThread()){
                lock.unlock();
                log.debug("Released lock for SKU : {} ",sku);
            }
        }
    }

    @Override
    public void releaseStock(String sku,int quantity){
        String lockKey = LOCK_PREFIX + sku;
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;
        try{
            isLocked = lock.tryLock(WAIT_TIME_SECONDS,LEASE_TIME_SECONDS,TimeUnit.SECONDS);
            if(!isLocked){
                log.warn("Unable to get lock for SKU {} within {}s",sku,WAIT_TIME_SECONDS);
                throw new LockAcquisitionException("System is busy handling requests for SKU"+ sku +"Please try again");
            }
            log.debug("Acquired lock for sku {} ",sku);
            transactionTemplate.executeWithoutResult(status -> {
                Inventory inventory = inventoryRepository.findById(sku).orElseThrow(() -> new InventoryNotFoundException("Couldn't find the requested item " + sku));
                inventory.release(quantity);
                inventoryRepository.save(inventory);
            });
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(isLocked && lock.isHeldByCurrentThread()){
                lock.unlock();
                log.debug("Released lock for SKU : {} ",sku);
            }
        }
    }
}
