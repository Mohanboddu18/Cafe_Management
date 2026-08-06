package com.cafe.management.service;

import com.cafe.management.entity.*;
import com.cafe.management.exception.ResourceNotFoundException;
import com.cafe.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    public Cart getOrCreateCart(String sessionId, Long tableId) {
        return cartRepository.findBySessionId(sessionId).orElseGet(() -> {
            RestaurantTable table = tableRepository.findById(tableId)
                    .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
            Cart cart = Cart.builder()
                    .sessionId(sessionId)
                    .table(table)
                    .build();
            return cartRepository.save(cart);
        });
    }

    @Transactional
    public Cart addItemToCart(String sessionId, Long tableId, Long menuItemId, Integer quantity, String notes) {
        Cart cart = getOrCreateCart(sessionId, tableId);
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu Item not found"));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItemId))
                .findFirst();

        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + quantity);
            if (notes != null) ci.setNotes(notes);
            cartItemRepository.save(ci);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(quantity)
                    .notes(notes)
                    .build();
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateCartItemQuantity(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart Item not found"));
        Cart cart = cartItem.getCart();

        if (quantity <= 0) {
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String sessionId) {
        cartRepository.findBySessionId(sessionId).ifPresent(cartRepository::delete);
    }
}
