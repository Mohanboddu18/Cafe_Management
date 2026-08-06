package com.cafe.management.service;

import com.cafe.management.entity.Inventory;
import com.cafe.management.entity.StockHistory;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.InventoryRepository;
import com.cafe.management.repository.StockHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    public List<Inventory> getLowStockAlerts() {
        return inventoryRepository.findLowStockItems();
    }

    public Inventory createInventoryItem(Inventory item) {
        return inventoryRepository.save(item);
    }

    @Transactional
    public Inventory updateStock(Long id, BigDecimal quantityChange, String transactionType, String notes) {
        Inventory item = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory item not found"));

        BigDecimal newStock = item.getCurrentStock();
        if ("IN".equalsIgnoreCase(transactionType)) {
            newStock = newStock.add(quantityChange);
        } else if ("OUT".equalsIgnoreCase(transactionType)) {
            newStock = newStock.subtract(quantityChange);
        } else if ("ADJUSTMENT".equalsIgnoreCase(transactionType)) {
            newStock = quantityChange;
        }

        if (newStock.compareTo(BigDecimal.ZERO) < 0) newStock = BigDecimal.ZERO;
        item.setCurrentStock(newStock);

        Inventory saved = inventoryRepository.save(item);

        StockHistory history = StockHistory.builder()
                .inventory(saved)
                .transactionType(transactionType.toUpperCase())
                .quantity(quantityChange)
                .notes(notes)
                .build();
        stockHistoryRepository.save(history);

        // Low stock notification alert
        if (saved.getCurrentStock().compareTo(saved.getMinRequiredStock()) <= 0) {
            notificationService.sendNotification(
                    "ADMIN",
                    "Low Stock Alert!",
                    "Item '" + saved.getItemName() + "' stock is low (" + saved.getCurrentStock() + " " + saved.getUnit() + " remaining).",
                    null,
                    null
            );
        }

        return saved;
    }

    public List<StockHistory> getStockHistory(Long inventoryId) {
        return stockHistoryRepository.findByInventoryIdOrderByCreatedAtDesc(inventoryId);
    }
}
