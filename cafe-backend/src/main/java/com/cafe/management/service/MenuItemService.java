package com.cafe.management.service;

import com.cafe.management.entity.Category;
import com.cafe.management.entity.MenuItem;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.CategoryRepository;
import com.cafe.management.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    public List<MenuItem> getAvailableMenuItems() {
        return menuItemRepository.findByIsAvailableTrue();
    }

    public List<MenuItem> getMenuItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    public List<MenuItem> searchMenuItems(String query) {
        return menuItemRepository.searchMenuItems(query);
    }

    public MenuItem createMenuItem(MenuItem item, Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        item.setCategory(category);
        return menuItemRepository.save(item);
    }

    public MenuItem updateMenuItem(Long id, MenuItem details, Long categoryId) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found"));
        if (categoryId != null) {
            Category cat = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            item.setCategory(cat);
        }
        item.setName(details.getName());
        item.setDescription(details.getDescription());
        item.setPrice(details.getPrice());
        item.setImageUrl(details.getImageUrl());
        item.setPrepTimeMins(details.getPrepTimeMins());
        item.setIsVeg(details.getIsVeg());
        item.setIsAvailable(details.getIsAvailable());
        item.setIsFeatured(details.getIsFeatured());
        return menuItemRepository.save(item);
    }

    public void toggleAvailability(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found"));
        item.setIsAvailable(!item.getIsAvailable());
        menuItemRepository.save(item);
    }

    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
}
