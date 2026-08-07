package com.cafe.management.config;

import com.cafe.management.entity.*;
import com.cafe.management.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RestaurantTableRepository tableRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [DataInitializer] Initializing cafe management database...");

        // 1. Ensure Roles exist
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").description("Admin").build()));
        Role waiterRole = roleRepository.findByName("ROLE_WAITER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_WAITER").description("Waiter").build()));
        Role kitchenRole = roleRepository.findByName("ROLE_KITCHEN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_KITCHEN").description("Kitchen").build()));
        Role cashierRole = roleRepository.findByName("ROLE_CASHIER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_CASHIER").description("Cashier").build()));

        // 2. Initialize / Update Staff Passwords
        createOrUpdateUser("admin", "admin123", "admin@cafemanagement.com", "System Admin", adminRole);
        createOrUpdateUser("waiter", "waiter123", "waiter@cafemanagement.com", "John Waiter", waiterRole);
        createOrUpdateUser("kitchen", "kitchen123", "kitchen@cafemanagement.com", "Chef Gordon", kitchenRole);
        createOrUpdateUser("cashier", "cashier123", "cashier@cafemanagement.com", "Sarah Cashier", cashierRole);

        // 3. Clear Stale Notifications from MySQL so all alerts are 100% live
        notificationRepository.deleteAll();
        System.out.println(">>> [DataInitializer] Stale notifications purged successfully!");

        // 4. Seed Restaurant Tables if empty
        if (tableRepository.count() == 0) {
            System.out.println(">>> [DataInitializer] Seeding restaurant tables...");
            int[] capacities = {2, 4, 4, 6, 2, 8, 4, 4};
            for (int i = 1; i <= 8; i++) {
                tableRepository.save(RestaurantTable.builder()
                        .tableNumber(i)
                        .capacity(capacities[i - 1])
                        .status("AVAILABLE")
                        .qrToken("TBL-QR-00" + i)
                        .qrCodeUrl("http://localhost:4200/customer/menu?table=" + i)
                        .build());
            }
            System.out.println(">>> [DataInitializer] Seeded 8 restaurant tables.");
        }

        // 5. Seed Categories & Menu Items if empty
        if (categoryRepository.count() == 0) {
            System.out.println(">>> [DataInitializer] Seeding categories and menu items...");

            Category cat1 = categoryRepository.save(Category.builder()
                    .name("Espresso & Coffee")
                    .description("Freshly roasted single-origin espresso and handcrafted coffee specialties")
                    .imageUrl("https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(1).active(true).build());

            Category cat2 = categoryRepository.save(Category.builder()
                    .name("Tea & Cold Drinks")
                    .description("Organic herbal teas, iced brews, fresh juices, and smoothies")
                    .imageUrl("https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(2).active(true).build());

            Category cat3 = categoryRepository.save(Category.builder()
                    .name("Artisanal Bakery & Toast")
                    .description("Freshly baked sourdough, croissants, and gourmet avocado toasts")
                    .imageUrl("https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(3).active(true).build());

            Category cat4 = categoryRepository.save(Category.builder()
                    .name("Breakfast & Pancakes")
                    .description("Fluffy buttermilk pancakes, Benedicts, and morning bowls")
                    .imageUrl("https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(4).active(true).build());

            Category cat5 = categoryRepository.save(Category.builder()
                    .name("Gourmet Sandwiches & Burgers")
                    .description("Brioche burgers, artisan paninis, and wholesome wraps")
                    .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(5).active(true).build());

            Category cat6 = categoryRepository.save(Category.builder()
                    .name("Desserts & Sweets")
                    .description("Decadent mousse, tarts, and handcrafted gelato")
                    .imageUrl("https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80")
                    .displayOrder(6).active(true).build());

            // Menu Items
            menuItemRepository.save(MenuItem.builder().category(cat1).name("Classic Double Espresso").description("Rich 100% Arabica double shot with velvety crema").price(new BigDecimal("3.50")).imageUrl("https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=600&q=80").prepTimeMins(5).isVeg(true).isAvailable(true).isFeatured(true).averageRating(4.8).totalRatings(12).build());
            menuItemRepository.save(MenuItem.builder().category(cat1).name("Caramel Cloud Cappuccino").description("Espresso steamed milk topped with salted caramel foam").price(new BigDecimal("4.90")).imageUrl("https://images.unsplash.com/photo-1572442388796-11668a67e53d?auto=format&fit=crop&w=600&q=80").prepTimeMins(7).isVeg(true).isAvailable(true).isFeatured(true).averageRating(4.9).totalRatings(25).build());
            menuItemRepository.save(MenuItem.builder().category(cat1).name("Iced Vanilla Bean Latte").description("Cold brewed espresso with Madagascar vanilla bean syrup and oat milk").price(new BigDecimal("5.50")).imageUrl("https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80").prepTimeMins(6).isVeg(true).isAvailable(true).isFeatured(false).averageRating(4.7).totalRatings(18).build());

            menuItemRepository.save(MenuItem.builder().category(cat2).name("Japanese Iced Matcha Latte").description("Ceremonial grade Uji matcha whisked with almond milk and honey").price(new BigDecimal("5.80")).imageUrl("https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80").prepTimeMins(6).isVeg(true).isAvailable(true).isFeatured(true).averageRating(4.9).totalRatings(30).build());
            menuItemRepository.save(MenuItem.builder().category(cat2).name("Fresh Passion Fruit Lemonade").description("Squeezed lemons infused with natural passion fruit nectar").price(new BigDecimal("4.20")).imageUrl("https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80").prepTimeMins(5).isVeg(true).isAvailable(true).isFeatured(false).averageRating(4.6).totalRatings(15).build());

            menuItemRepository.save(MenuItem.builder().category(cat3).name("Sourdough Avocado Toast").description("Smashed Hass avocado, poached egg, cherry tomatoes & feta crumble").price(new BigDecimal("9.50")).imageUrl("https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?auto=format&fit=crop&w=600&q=80").prepTimeMins(12).isVeg(true).isAvailable(true).isFeatured(true).averageRating(4.8).totalRatings(22).build());
            menuItemRepository.save(MenuItem.builder().category(cat3).name("Butter Almond Croissant").description("Flaky French butter pastry filled with toasted almond cream").price(new BigDecimal("4.50")).imageUrl("https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80").prepTimeMins(5).isVeg(true).isAvailable(true).isFeatured(false).averageRating(4.7).totalRatings(19).build());

            menuItemRepository.save(MenuItem.builder().category(cat4).name("Blueberry Maple Pancake Stack").description("Fluffy triple-stacked pancakes with wild blueberry compote and warm maple syrup").price(new BigDecimal("10.90")).imageUrl("https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80").prepTimeMins(15).isVeg(true).isAvailable(true).isFeatured(true).averageRating(5.0).totalRatings(35).build());

            menuItemRepository.save(MenuItem.builder().category(cat5).name("Truffle Mushroom Angus Burger").description("100% Angus beef patty, Swiss cheese, sauteed mushrooms & truffle aioli").price(new BigDecimal("14.50")).imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80").prepTimeMins(18).isVeg(false).isAvailable(true).isFeatured(true).averageRating(4.9).totalRatings(40).build());
            menuItemRepository.save(MenuItem.builder().category(cat5).name("Crispy Paneer Tikka Wrap").description("Grilled marinated cottage cheese, crunchy greens & mint chutney wrap").price(new BigDecimal("11.20")).imageUrl("https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=600&q=80").prepTimeMins(14).isVeg(true).isAvailable(true).isFeatured(false).averageRating(4.8).totalRatings(16).build());

            menuItemRepository.save(MenuItem.builder().category(cat6).name("Belgian Chocolate Lava Cake").description("Warm molten chocolate cake served with Madagascar vanilla gelato").price(new BigDecimal("7.50")).imageUrl("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80").prepTimeMins(10).isVeg(true).isAvailable(true).isFeatured(true).averageRating(4.9).totalRatings(28).build());

            System.out.println(">>> [DataInitializer] Seeded categories and menu items.");
        }

        // 6. Seed Coupons if empty
        if (couponRepository.count() == 0) {
            System.out.println(">>> [DataInitializer] Seeding coupons...");
            couponRepository.save(Coupon.builder().code("WELCOME10").description("10% discount on orders over $15").discountType("PERCENTAGE").discountValue(new BigDecimal("10.00")).minOrderAmount(new BigDecimal("15.00")).maxDiscount(new BigDecimal("10.00")).validUntil(LocalDate.of(2027, 12, 31)).active(true).build());
            couponRepository.save(Coupon.builder().code("FLAT50").description("Flat $5 off on orders over $25").discountType("FLAT").discountValue(new BigDecimal("5.00")).minOrderAmount(new BigDecimal("25.00")).maxDiscount(new BigDecimal("5.00")).validUntil(LocalDate.of(2027, 12, 31)).active(true).build());
            couponRepository.save(Coupon.builder().code("CAFE20").description("20% off for special celebrations").discountType("PERCENTAGE").discountValue(new BigDecimal("20.00")).minOrderAmount(new BigDecimal("30.00")).maxDiscount(new BigDecimal("15.00")).validUntil(LocalDate.of(2027, 12, 31)).active(true).build());
            System.out.println(">>> [DataInitializer] Seeded coupons.");
        }

        // 7. Seed Inventory if empty
        if (inventoryRepository.count() == 0) {
            System.out.println(">>> [DataInitializer] Seeding inventory...");
            inventoryRepository.save(Inventory.builder().itemName("Single Origin Arabica Beans").unit("KG").currentStock(new BigDecimal("25.50")).minRequiredStock(new BigDecimal("5.00")).costPerUnit(new BigDecimal("18.00")).build());
            inventoryRepository.save(Inventory.builder().itemName("Whole Organic Milk").unit("LITRE").currentStock(new BigDecimal("40.00")).minRequiredStock(new BigDecimal("10.00")).costPerUnit(new BigDecimal("2.50")).build());
            inventoryRepository.save(Inventory.builder().itemName("Oat Milk").unit("LITRE").currentStock(new BigDecimal("18.00")).minRequiredStock(new BigDecimal("5.00")).costPerUnit(new BigDecimal("3.80")).build());
            inventoryRepository.save(Inventory.builder().itemName("Artisanal Sourdough Loaf").unit("PACKET").currentStock(new BigDecimal("12.00")).minRequiredStock(new BigDecimal("4.00")).costPerUnit(new BigDecimal("4.00")).build());
            inventoryRepository.save(Inventory.builder().itemName("Hass Avocado").unit("PCS").currentStock(new BigDecimal("45.00")).minRequiredStock(new BigDecimal("15.00")).costPerUnit(new BigDecimal("1.20")).build());
            inventoryRepository.save(Inventory.builder().itemName("Angus Beef Patties").unit("PCS").currentStock(new BigDecimal("30.00")).minRequiredStock(new BigDecimal("10.00")).costPerUnit(new BigDecimal("3.50")).build());
            inventoryRepository.save(Inventory.builder().itemName("Ceremonial Matcha Powder").unit("KG").currentStock(new BigDecimal("3.20")).minRequiredStock(new BigDecimal("1.00")).costPerUnit(new BigDecimal("45.00")).build());
            inventoryRepository.save(Inventory.builder().itemName("French Butter Croissant").unit("PCS").currentStock(new BigDecimal("20.00")).minRequiredStock(new BigDecimal("5.00")).costPerUnit(new BigDecimal("1.50")).build());
            System.out.println(">>> [DataInitializer] Seeded inventory.");
        }
    }

    private void createOrUpdateUser(String username, String rawPassword, String email, String fullName, Role role) {
        User user = userRepository.findByUsername(username).orElseGet(() ->
                User.builder()
                        .username(username)
                        .email(email)
                        .fullName(fullName)
                        .role(role)
                        .status("ACTIVE")
                        .build()
        );

        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        user.setStatus("ACTIVE");
        userRepository.save(user);
        System.out.println(">>> [DataInitializer] User '" + username + "' password updated for login (" + rawPassword + ")");
    }
}

