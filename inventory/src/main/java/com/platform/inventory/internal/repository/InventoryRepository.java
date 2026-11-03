package com.platform.inventory.internal.repository;

import com.platform.inventory.internal.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory,String> {

}
