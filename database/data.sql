-- =========================================================
-- CAFE MANAGEMENT SYSTEM - SEED DATA (MySQL 8.0)
-- =========================================================

USE `cafe_management`;
-- 1. SEED ROLES
INSERT INTO `roles` (`id`, `name`, `description`) VALUES
(1, 'ROLE_ADMIN', 'System Administrator with full access'),
(2, 'ROLE_WAITER', 'Floor Waiter managing tables and order delivery'),
(3, 'ROLE_KITCHEN', 'Chef and Kitchen Staff managing food preparation'),
(4, 'ROLE_CASHIER', 'Cashier managing billing, discounts, and payments')
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 2. SEED USERS (Passwords hashed using BCrypt: admin123, waiter123, kitchen123, cashier123)
-- BCrypt hash for 'admin123': '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu'
-- BCrypt hash for 'waiter123': '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu'
-- BCrypt hash for 'kitchen123': '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu'
-- BCrypt hash for 'cashier123': '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu'
INSERT INTO `users` (`id`, `username`, `password`, `email`, `full_name`, `phone`, `role_id`, `status`) VALUES
(1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'admin@cafemanagement.com', 'System Admin', '+1 555-0100', 1, 'ACTIVE'),
(2, 'waiter', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'waiter@cafemanagement.com', 'John Waiter', '+1 555-0101', 2, 'ACTIVE'),
(3, 'kitchen', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'kitchen@cafemanagement.com', 'Chef Gordon', '+1 555-0102', 3, 'ACTIVE'),
(4, 'cashier', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'cashier@cafemanagement.com', 'Sarah Cashier', '+1 555-0103', 4, 'ACTIVE')
ON DUPLICATE KEY UPDATE `username`=`username`;

-- 3. SEED RESTAURANT TABLES
INSERT INTO `restaurant_tables` (`id`, `table_number`, `capacity`, `status`, `qr_token`, `qr_code_url`) VALUES
(1, 1, 2, 'AVAILABLE', 'TBL-QR-001', 'http://localhost:4200/customer/menu?table=1'),
(2, 2, 4, 'AVAILABLE', 'TBL-QR-002', 'http://localhost:4200/customer/menu?table=2'),
(3, 3, 4, 'AVAILABLE', 'TBL-QR-003', 'http://localhost:4200/customer/menu?table=3'),
(4, 4, 6, 'AVAILABLE', 'TBL-QR-004', 'http://localhost:4200/customer/menu?table=4'),
(5, 5, 2, 'AVAILABLE', 'TBL-QR-005', 'http://localhost:4200/customer/menu?table=5'),
(6, 6, 8, 'AVAILABLE', 'TBL-QR-006', 'http://localhost:4200/customer/menu?table=6'),
(7, 7, 4, 'AVAILABLE', 'TBL-QR-007', 'http://localhost:4200/customer/menu?table=7'),
(8, 8, 4, 'AVAILABLE', 'TBL-QR-008', 'http://localhost:4200/customer/menu?table=8')
ON DUPLICATE KEY UPDATE `table_number`=`table_number`;

-- 4. SEED CATEGORIES
INSERT INTO `categories` (`id`, `name`, `description`, `image_url`, `display_order`, `active`) VALUES
(1, 'Espresso & Coffee', 'Freshly roasted single-origin espresso and handcrafted coffee specialties', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80', 1, TRUE),
(2, 'Tea & Cold Drinks', 'Organic herbal teas, iced brews, fresh juices, and smoothies', 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80', 2, TRUE),
(3, 'Artisanal Bakery & Toast', 'Freshly baked sourdough, croissants, and gourmet avocado toasts', 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80', 3, TRUE),
(4, 'Breakfast & Egg Specialties', 'Fluffy buttermilk pancakes, Benedicts, and gourmet egg bowls', 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80', 4, TRUE),
(5, 'Gourmet Sandwiches & Burgers', 'Brioche burgers, artisan paninis, and wholesome wraps', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80', 5, TRUE),
(6, 'Artisanal Wood-Fired Pizzas', 'Hand-tossed sourdough pizzas with premium toppings', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80', 6, TRUE),
(7, 'Authentic Shawarmas & Wraps', 'Middle Eastern grilled shawarmas and spiced wraps', 'https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&w=600&q=80', 7, TRUE),
(8, 'Mocktails & Mixology', 'Handcrafted non-alcoholic cocktails & sparkling coolers', 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80', 8, TRUE),
(9, 'Fresh Fruit Juices & Cool Drinks', '100% cold-pressed juices and chilled refreshing drinks', 'https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80', 9, TRUE),
(10, 'Desserts & Sweets', 'Decadent mousse, tarts, and handcrafted gelato', 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80', 10, TRUE)
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 5. SEED MENU ITEMS
INSERT INTO `menu_items` (`id`, `category_id`, `name`, `description`, `price`, `image_url`, `prep_time_mins`, `is_veg`, `is_available`, `is_featured`) VALUES
-- 1. Coffee
(1, 1, 'Classic Double Espresso', 'Rich 100% Arabica double shot with velvety crema', 3.50, 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(2, 1, 'Caramel Cloud Cappuccino', 'Espresso steamed milk topped with salted caramel foam', 4.90, 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?auto=format&fit=crop&w=600&q=80', 7, TRUE, TRUE, TRUE),
(3, 1, 'Iced Vanilla Bean Latte', 'Cold brewed espresso with Madagascar vanilla bean syrup and oat milk', 5.50, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, FALSE),
-- 2. Tea
(4, 2, 'Japanese Iced Matcha Latte', 'Ceremonial grade Uji matcha whisked with almond milk and honey', 5.80, 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, TRUE),
(5, 2, 'Fresh Passion Fruit Lemonade', 'Squeezed lemons infused with natural passion fruit nectar', 4.20, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
-- 3. Bakery
(6, 3, 'Sourdough Avocado Toast', 'Smashed Hass avocado, poached egg, cherry tomatoes & feta crumble', 9.50, 'https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, TRUE),
(7, 3, 'Butter Almond Croissant', 'Flaky French butter pastry filled with toasted almond cream', 4.50, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
-- 4. Breakfast & Eggs
(8, 4, 'Blueberry Maple Pancake Stack', 'Fluffy triple-stacked pancakes with wild blueberry compote and warm maple syrup', 10.90, 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, TRUE),
(9, 4, 'Truffle Scrambled Eggs on Sourdough', 'Creamy organic scrambled eggs infused with black truffle oil on toasted sourdough', 11.50, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=600&q=80', 10, FALSE, TRUE, TRUE),
(10, 4, 'Classic Eggs Benedict with Smoked Turkey', 'Poached eggs, smoked turkey slice & hollandaise sauce on English muffin', 13.20, 'https://images.unsplash.com/photo-1608039829572-78524f79c4c7?auto=format&fit=crop&w=600&q=80', 12, FALSE, TRUE, FALSE),
(11, 4, 'Cheese & Herb Omelette', 'Three-egg fluffy omelette packed with sharp cheddar, mozzarella, and fresh herbs', 9.80, 'https://images.unsplash.com/photo-1510693206972-df098062cb71?auto=format&fit=crop&w=600&q=80', 10, FALSE, TRUE, FALSE),
-- 5. Burgers
(12, 5, 'Truffle Mushroom Angus Burger', '100% Angus beef patty, Swiss cheese, sauteed mushrooms & truffle aioli', 14.50, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80', 18, FALSE, TRUE, TRUE),
(13, 5, 'Crispy Fiery Chicken Burger', 'Spicy buttermilk fried chicken breast, coleslaw & jalapeno mayo on brioche', 12.90, 'https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?auto=format&fit=crop&w=600&q=80', 15, FALSE, TRUE, TRUE),
(14, 5, 'Double Cheese Smash Burger', 'Two seared beef patties, American cheese, caramelized onions & secret sauce', 13.80, 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=600&q=80', 14, FALSE, TRUE, FALSE),
(15, 5, 'Classic Veggie Loaded Burger', 'Crispy spiced potato & corn patty with cheddar cheese & herb mayo', 10.50, 'https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, FALSE),
-- 6. Pizzas
(16, 6, 'Artisan Margherita Pizza', 'Wood-fired sourdough base with San Marzano tomato sauce, fresh mozzarella & basil', 12.50, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, TRUE),
(17, 6, 'Spicy Pepperoni Feast Pizza', 'Crispy wood-fired crust loaded with Italian pepperoni, mozzarella & chili flakes', 15.90, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=600&q=80', 16, FALSE, TRUE, TRUE),
(18, 6, 'Truffle Mushroom & Spinach Pizza', 'Roasted wild mushrooms, ricotta cheese, truffle drizzle & fresh baby spinach', 14.80, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, FALSE),
(19, 6, 'BBQ Loaded Chicken Pizza', 'Smoky BBQ chicken, red onions, bell peppers & cilantro on mozzarella base', 15.20, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=600&q=80', 16, FALSE, TRUE, TRUE),
-- 7. Shawarmas
(20, 7, 'Classic Lebanese Chicken Shawarma', 'Slow-roasted chicken shawarma, garlic toum, pickles & crispy fries wrapped in pita', 9.90, 'https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&w=600&q=80', 10, FALSE, TRUE, TRUE),
(21, 7, 'Spicy Lamb Shawarma Roll', 'Tender spiced lamb strips, tahini, sumac onions & fresh parsley in saj bread', 12.50, 'https://images.unsplash.com/photo-1603360946369-dc9bb6258143?auto=format&fit=crop&w=600&q=80', 12, FALSE, TRUE, TRUE),
(22, 7, 'Grilled Paneer Tikka Shawarma', 'Char-grilled spiced cottage cheese, mint yogurt sauce & crunchy veggies in flatbread', 8.90, 'https://images.unsplash.com/photo-1626700051175-6818013e1d4f?auto=format&fit=crop&w=600&q=80', 10, TRUE, TRUE, FALSE),
-- 8. Mocktails
(23, 8, 'Virgin Blue Ocean Mojito', 'Blue curacao, muddled fresh mint, lime juice & sparkling soda on ice', 6.50, 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(24, 8, 'Watermelon Mint Cooler', 'Fresh crushed watermelon, cooling mint leaves & lemon twist sparkler', 5.90, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(25, 8, 'Tropical Passion Fruit Sparkler', 'Exotic passion fruit nectar, pineapple juice & sparkling tonic water', 6.20, 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
-- 9. Fruit Juices & Cool Drinks
(26, 9, 'Cold-Pressed Valencia Orange Juice', '100% pure freshly squeezed orange juice packed with natural Vitamin C', 4.80, 'https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80', 4, TRUE, TRUE, TRUE),
(27, 9, 'Detox Green Apple & Ginger Juice', 'Crisp green apple, cucumber, celery & fresh ginger root extract', 5.20, 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(28, 9, 'Chilled Ice Cold Cola', 'Refreshing classic carbonated cola served with fresh ice & lemon wedge', 2.50, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=600&q=80', 2, TRUE, TRUE, FALSE),
-- 10. Desserts
(29, 10, 'Belgian Chocolate Lava Cake', 'Warm molten chocolate cake served with Madagascar vanilla gelato', 7.50, 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80', 10, TRUE, TRUE, TRUE)
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 6. SEED COUPONS
INSERT INTO `coupons` (`id`, `code`, `description`, `discount_type`, `discount_value`, `min_order_amount`, `max_discount`, `valid_until`, `active`) VALUES
(1, 'WELCOME10', '10% discount on orders over $15', 'PERCENTAGE', 10.00, 15.00, 10.00, '2027-12-31', TRUE),
(2, 'FLAT50', 'Flat $5 off on orders over $25', 'FLAT', 5.00, 25.00, 5.00, '2027-12-31', TRUE),
(3, 'CAFE20', '20% off for special celebrations', 'PERCENTAGE', 20.00, 30.00, 15.00, '2027-12-31', TRUE)
ON DUPLICATE KEY UPDATE `code`=`code`;

-- 7. SEED INVENTORY
INSERT INTO `inventory` (`id`, `item_name`, `unit`, `current_stock`, `min_required_stock`, `cost_per_unit`) VALUES
(1, 'Single Origin Arabica Beans', 'KG', 25.50, 5.00, 18.00),
(2, 'Whole Organic Milk', 'LITRE', 40.00, 10.00, 2.50),
(3, 'Oat Milk', 'LITRE', 18.00, 5.00, 3.80),
(4, 'Artisanal Sourdough Loaf', 'PACKET', 12.00, 4.00, 4.00),
(5, 'Hass Avocado', 'PCS', 45.00, 15.00, 1.20),
(6, 'Angus Beef Patties', 'PCS', 30.00, 10.00, 3.50),
(7, 'Ceremonial Matcha Powder', 'KG', 3.20, 1.00, 45.00),
(8, 'French Butter Croissant', 'PCS', 20.00, 5.00, 1.50)
ON DUPLICATE KEY UPDATE `item_name`=`item_name`;

-- 8. SEED NOTIFICATIONS (NONE - ALL NOTIFICATIONS GENERATED DYNAMICALLY FROM MYSQL)

