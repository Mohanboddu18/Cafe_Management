package com.cafe.management.service;

import com.cafe.management.entity.Category;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category category) {
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category details) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        cat.setName(details.getName());
        cat.setDescription(details.getDescription());
        cat.setImageUrl(details.getImageUrl());
        cat.setDisplayOrder(details.getDisplayOrder());
        cat.setActive(details.getActive());
        return categoryRepository.save(cat);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
