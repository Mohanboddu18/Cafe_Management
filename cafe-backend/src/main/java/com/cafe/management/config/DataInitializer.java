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

        // 5. Seed / Ensure Categories & Menu Items ONLY if empty
        if (categoryRepository.count() == 0) {
            System.out.println(">>> [DataInitializer] Seeding initial categories and menu items...");

            Category cat1 = createOrUpdateCategory("Espresso & Coffee", "Freshly roasted single-origin espresso and handcrafted coffee specialties", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80", 1);
            Category cat2 = createOrUpdateCategory("Tea & Cold Drinks", "Organic herbal teas, iced brews, fresh juices, and smoothies", "https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80", 2);
            Category cat3 = createOrUpdateCategory("Artisanal Bakery & Toast", "Freshly baked sourdough, croissants, and gourmet avocado toasts", "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80", 3);
            Category cat4 = createOrUpdateCategory("Breakfast & Egg Specialties", "Fluffy buttermilk pancakes, Benedicts, and gourmet egg bowls", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80", 4);
            Category cat5 = createOrUpdateCategory("Gourmet Sandwiches & Burgers", "Brioche burgers, artisan paninis, and wholesome wraps", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80", 5);
            Category cat6 = createOrUpdateCategory("Artisanal Wood-Fired Pizzas", "Hand-tossed sourdough pizzas with premium toppings", "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80", 6);
            Category cat7 = createOrUpdateCategory("Authentic Shawarmas & Wraps", "Middle Eastern grilled shawarmas and spiced wraps", "https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&w=600&q=80", 7);
            Category cat8 = createOrUpdateCategory("Mocktails & Mixology", "Handcrafted non-alcoholic cocktails & sparkling coolers", "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80", 8);
            Category cat9 = createOrUpdateCategory("Fresh Fruit Juices & Cool Drinks", "100% cold-pressed juices and chilled refreshing drinks", "https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80", 9);
            Category cat10 = createOrUpdateCategory("Desserts & Sweets", "Decadent mousse, tarts, and handcrafted gelato", "https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80", 10);

            // 1. Espresso & Coffee Items
            createOrUpdateMenuItem(cat1, "Classic Double Espresso", "Rich 100% Arabica double shot with velvety crema", new BigDecimal("3.50"), "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=600&q=80", 5, true, true, true, 4.8, 12);
            createOrUpdateMenuItem(cat1, "Caramel Cloud Cappuccino", "Espresso steamed milk topped with salted caramel foam", new BigDecimal("4.90"), "https://images.unsplash.com/photo-1572442388796-11668a67e53d?auto=format&fit=crop&w=600&q=80", 7, true, true, true, 4.9, 25);
            createOrUpdateMenuItem(cat1, "Iced Vanilla Bean Latte", "Cold brewed espresso with Madagascar vanilla bean syrup and oat milk", new BigDecimal("5.50"), "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80", 6, true, true, false, 4.7, 18);

            // 2. Tea & Cold Drinks
            createOrUpdateMenuItem(cat2, "Japanese Iced Matcha Latte", "Ceremonial grade Uji matcha whisked with almond milk and honey", new BigDecimal("5.80"), "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80", 6, true, true, true, 4.9, 30);
            createOrUpdateMenuItem(cat2, "Fresh Passion Fruit Lemonade", "Squeezed lemons infused with natural passion fruit nectar", new BigDecimal("4.20"), "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80", 5, true, true, false, 4.6, 15);

            // 3. Bakery & Toast
            createOrUpdateMenuItem(cat3, "Sourdough Avocado Toast", "Smashed Hass avocado, poached egg, cherry tomatoes & feta crumble", new BigDecimal("9.50"), "https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?auto=format&fit=crop&w=600&q=80", 12, true, true, true, 4.8, 22);
            createOrUpdateMenuItem(cat3, "Butter Almond Croissant", "Flaky French butter pastry filled with toasted almond cream", new BigDecimal("4.50"), "https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80", 5, true, true, false, 4.7, 19);

            // 4. Breakfast & Egg Specialties
            createOrUpdateMenuItem(cat4, "Blueberry Maple Pancake Stack", "Fluffy triple-stacked pancakes with wild blueberry compote and warm maple syrup", new BigDecimal("10.90"), "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80", 15, true, true, true, 5.0, 35);
            createOrUpdateMenuItem(cat4, "Truffle Scrambled Eggs on Sourdough", "Creamy organic scrambled eggs infused with black truffle oil on toasted sourdough", new BigDecimal("11.50"), "https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=600&q=80", 10, false, true, true, 4.9, 20);
            createOrUpdateMenuItem(cat4, "Classic Eggs Benedict with Smoked Turkey", "Poached eggs, smoked turkey slice & hollandaise sauce on English muffin", new BigDecimal("13.20"), "https://images.unsplash.com/photo-1608039829572-78524f79c4c7?auto=format&fit=crop&w=600&q=80", 12, false, true, false, 4.8, 14);
            createOrUpdateMenuItem(cat4, "Cheese & Herb Omelette", "Three-egg fluffy omelette packed with sharp cheddar, mozzarella, and fresh herbs", new BigDecimal("9.80"), "https://images.unsplash.com/photo-1510693206972-df098062cb71?auto=format&fit=crop&w=600&q=80", 10, false, true, false, 4.7, 16);

            // 5. Gourmet Burgers
            createOrUpdateMenuItem(cat5, "Truffle Mushroom Angus Burger", "100% Angus beef patty, Swiss cheese, sauteed mushrooms & truffle aioli", new BigDecimal("14.50"), "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80", 18, false, true, true, 4.9, 40);
            createOrUpdateMenuItem(cat5, "Crispy Fiery Chicken Burger", "Spicy buttermilk fried chicken breast, coleslaw & jalapeno mayo on brioche", new BigDecimal("12.90"), "https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?auto=format&fit=crop&w=600&q=80", 15, false, true, true, 4.8, 28);
            createOrUpdateMenuItem(cat5, "Double Cheese Smash Burger", "Two seared beef patties, American cheese, caramelized onions & secret sauce", new BigDecimal("13.80"), "https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=600&q=80", 14, false, true, false, 4.9, 32);
            createOrUpdateMenuItem(cat5, "Classic Veggie Loaded Burger", "Crispy spiced potato & corn patty with cheddar cheese & herb mayo", new BigDecimal("10.50"), "https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=600&q=80", 12, true, true, false, 4.6, 18);

            // 6. Artisanal Wood-Fired Pizzas
            createOrUpdateMenuItem(cat6, "Artisan Margherita Pizza", "Wood-fired sourdough base with San Marzano tomato sauce, fresh mozzarella & basil", new BigDecimal("12.50"), "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80", 15, true, true, true, 4.9, 45);
            createOrUpdateMenuItem(cat6, "Spicy Pepperoni Feast Pizza", "Crispy wood-fired crust loaded with Italian pepperoni, mozzarella & chili flakes", new BigDecimal("15.90"), "https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=600&q=80", 16, false, true, true, 4.9, 50);
            createOrUpdateMenuItem(cat6, "Truffle Mushroom & Spinach Pizza", "Roasted wild mushrooms, ricotta cheese, truffle drizzle & fresh baby spinach", new BigDecimal("14.80"), "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=600&q=80", 15, true, true, false, 4.8, 22);
            createOrUpdateMenuItem(cat6, "BBQ Loaded Chicken Pizza", "Smoky BBQ chicken, red onions, bell peppers & cilantro on mozzarella base", new BigDecimal("15.20"), "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=600&q=80", 16, false, true, true, 4.9, 38);

            // 7. Authentic Shawarmas & Wraps
            createOrUpdateMenuItem(cat7, "Classic Lebanese Chicken Shawarma", "Slow-roasted chicken shawarma, garlic toum, pickles & crispy fries wrapped in pita", new BigDecimal("9.90"), "https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&w=600&q=80", 10, false, true, true, 4.9, 42);
            createOrUpdateMenuItem(cat7, "Spicy Lamb Shawarma Roll", "Tender spiced lamb strips, tahini, sumac onions & fresh parsley in saj bread", new BigDecimal("12.50"), "https://images.unsplash.com/photo-1603360946369-dc9bb6258143?auto=format&fit=crop&w=600&q=80", 12, false, true, true, 4.8, 27);
            createOrUpdateMenuItem(cat7, "Grilled Paneer Tikka Shawarma", "Char-grilled spiced cottage cheese, mint yogurt sauce & crunchy veggies in flatbread", new BigDecimal("8.90"), "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=600&q=80", 10, true, true, false, 4.7, 16);

            // 8. Mocktails & Mixology
            createOrUpdateMenuItem(cat8, "Virgin Blue Ocean Mojito", "Blue curacao, muddled fresh mint, lime juice & sparkling soda on ice", new BigDecimal("6.50"), "https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80", 5, true, true, true, 4.9, 35);
            createOrUpdateMenuItem(cat8, "Watermelon Mint Cooler", "Fresh crushed watermelon, cooling mint leaves & lemon twist sparkler", new BigDecimal("5.90"), "https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80", 5, true, true, true, 4.8, 29);
            createOrUpdateMenuItem(cat8, "Tropical Passion Fruit Sparkler", "Exotic passion fruit nectar, pineapple juice & sparkling tonic water", new BigDecimal("6.20"), "https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80", 5, true, true, false, 4.7, 21);

            // 9. Fresh Fruit Juices & Cool Drinks
            createOrUpdateMenuItem(cat9, "Cold-Pressed Valencia Orange Juice", "100% pure freshly squeezed orange juice packed with natural Vitamin C", new BigDecimal("4.80"), "https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80", 4, true, true, true, 4.9, 31);
            createOrUpdateMenuItem(cat9, "Detox Green Apple & Ginger Juice", "Crisp green apple, cucumber, celery & fresh ginger root extract", new BigDecimal("5.20"), "https://images.unsplash.com/photo-1600271886742-f049cd451bba?auto=format&fit=crop&w=600&q=80", 5, true, true, false, 4.6, 17);
            createOrUpdateMenuItem(cat9, "Chilled Ice Cold Cola", "Refreshing classic carbonated cola served with fresh ice & lemon wedge", new BigDecimal("2.50"), "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=600&q=80", 2, true, true, false, 4.7, 50);

            // 10. Desserts & Sweets
            createOrUpdateMenuItem(cat10, "Belgian Chocolate Lava Cake", "Warm molten chocolate cake served with Madagascar vanilla gelato", new BigDecimal("7.50"), "https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80", 10, true, true, true, 4.9, 28);

            System.out.println(">>> [DataInitializer] Seeded initial categories and menu items successfully!");
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

    private Category createOrUpdateCategory(String name, String description, String imageUrl, int displayOrder) {
        return categoryRepository.findByName(name).orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .imageUrl(imageUrl)
                        .displayOrder(displayOrder)
                        .active(true)
                        .build())
        );
    }

    private MenuItem createOrUpdateMenuItem(Category category, String name, String description, BigDecimal price, String imageUrl, int prepTimeMins, boolean isVeg, boolean isAvailable, boolean isFeatured, double avgRating, int totalRatings) {
        return menuItemRepository.findByName(name).orElseGet(() ->
                menuItemRepository.save(MenuItem.builder()
                        .category(category)
                        .name(name)
                        .description(description)
                        .price(price)
                        .imageUrl(imageUrl)
                        .prepTimeMins(prepTimeMins)
                        .isVeg(isVeg)
                        .isAvailable(isAvailable)
                        .isFeatured(isFeatured)
                        .averageRating(avgRating)
                        .totalRatings(totalRatings)
                        .build())
        );
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


