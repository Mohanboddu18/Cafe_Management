-- =========================================================
-- CAFE MANAGEMENT SYSTEM - DATABASE SCHEMA (MySQL 8.0)
-- =========================================================

-- CREATE DATABASE IF NOT EXISTS `cafe_management` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- USE `cafe_management`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `stock_history`;
DROP TABLE IF EXISTS `inventory`;
DROP TABLE IF EXISTS `payments`;
DROP TABLE IF EXISTS `invoices`;
DROP TABLE IF EXISTS `coupons`;
DROP TABLE IF EXISTS `kitchen_orders`;
DROP TABLE IF EXISTS `order_items`;
DROP TABLE IF EXISTS `orders`;
DROP TABLE IF EXISTS `cart_items`;
DROP TABLE IF EXISTS `cart`;
DROP TABLE IF EXISTS `customers`;
DROP TABLE IF EXISTS `menu_items`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `qr_codes`;
DROP TABLE IF EXISTS `restaurant_tables`;
DROP TABLE IF EXISTS `role_permissions`;
DROP TABLE IF EXISTS `permissions`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. ROLES
CREATE TABLE `roles` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. USERS
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20),
    `role_id` BIGINT NOT NULL,
    `status` VARCHAR(20) DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. PERMISSIONS
CREATE TABLE `permissions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. ROLE PERMISSIONS
CREATE TABLE `role_permissions` (
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. RESTAURANT TABLES
CREATE TABLE `restaurant_tables` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `table_number` INT NOT NULL UNIQUE,
    `capacity` INT NOT NULL DEFAULT 4,
    `status` VARCHAR(30) DEFAULT 'AVAILABLE', -- AVAILABLE, OCCUPIED, BILL_REQUESTED, RESERVED, CLEANING
    `qr_code_url` VARCHAR(500),
    `qr_token` VARCHAR(100) UNIQUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. QR CODES
CREATE TABLE `qr_codes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `table_id` BIGINT NOT NULL UNIQUE,
    `qr_data` VARCHAR(500) NOT NULL,
    `image_base64` LONGTEXT,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_qr_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_tables` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. CATEGORIES
CREATE TABLE `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` TEXT,
    `image_url` VARCHAR(500),
    `display_order` INT DEFAULT 0,
    `active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. MENU ITEMS
CREATE TABLE `menu_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `category_id` BIGINT NOT NULL,
    `name` VARCHAR(150) NOT NULL,
    `description` TEXT,
    `price` DECIMAL(10, 2) NOT NULL,
    `image_url` VARCHAR(500),
    `prep_time_mins` INT DEFAULT 15,
    `is_veg` BOOLEAN DEFAULT TRUE,
    `is_available` BOOLEAN DEFAULT TRUE,
    `is_featured` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_menu_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. CUSTOMERS
CREATE TABLE `customers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(100) NOT NULL,
    `name` VARCHAR(100),
    `phone` VARCHAR(20),
    `table_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_customer_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_tables` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. CART
CREATE TABLE `cart` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` VARCHAR(100) NOT NULL UNIQUE,
    `table_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cart_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_tables` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. CART ITEMS
CREATE TABLE `cart_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `cart_id` BIGINT NOT NULL,
    `menu_item_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `notes` VARCHAR(255),
    CONSTRAINT `fk_ci_cart` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ci_menu` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. ORDERS
CREATE TABLE `orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_number` VARCHAR(50) NOT NULL UNIQUE,
    `table_id` BIGINT NOT NULL,
    `customer_id` BIGINT,
    `total_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(10, 2) DEFAULT 0.00,
    `tax_amount` DECIMAL(10, 2) DEFAULT 0.00,
    `net_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `status` VARCHAR(30) NOT NULL DEFAULT 'NEW', -- NEW, ACCEPTED, PREPARING, READY, SERVED, COMPLETED, CANCELLED
    `notes` VARCHAR(255),
    `order_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_order_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_tables` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_order_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 13. ORDER ITEMS
CREATE TABLE `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `menu_item_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `unit_price` DECIMAL(10, 2) NOT NULL,
    `total_price` DECIMAL(10, 2) NOT NULL,
    `item_status` VARCHAR(30) DEFAULT 'PENDING', -- PENDING, PREPARING, READY, REJECTED, SERVED
    `notes` VARCHAR(255),
    CONSTRAINT `fk_oi_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_oi_menu` FOREIGN KEY (`menu_item_id`) REFERENCES `menu_items` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 14. KITCHEN ORDERS
CREATE TABLE `kitchen_orders` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL UNIQUE,
    `kitchen_status` VARCHAR(30) DEFAULT 'QUEUED', -- QUEUED, PREPARING, READY, COMPLETED
    `estimated_prep_time` INT DEFAULT 15,
    `started_at` TIMESTAMP NULL,
    `completed_at` TIMESTAMP NULL,
    CONSTRAINT `fk_ko_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 15. COUPONS
CREATE TABLE `coupons` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(30) NOT NULL UNIQUE,
    `description` VARCHAR(255),
    `discount_type` VARCHAR(20) NOT NULL, -- PERCENTAGE, FLAT
    `discount_value` DECIMAL(10, 2) NOT NULL,
    `min_order_amount` DECIMAL(10, 2) DEFAULT 0.00,
    `max_discount` DECIMAL(10, 2) DEFAULT 0.00,
    `valid_until` DATE NOT NULL,
    `active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 16. INVOICES
CREATE TABLE `invoices` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `invoice_number` VARCHAR(50) NOT NULL UNIQUE,
    `order_id` BIGINT NOT NULL UNIQUE,
    `subtotal` DECIMAL(10, 2) NOT NULL,
    `discount` DECIMAL(10, 2) DEFAULT 0.00,
    `coupon_code` VARCHAR(30),
    `gst_amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `total_payable` DECIMAL(10, 2) NOT NULL,
    `pdf_url` VARCHAR(500),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inv_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 17. PAYMENTS
CREATE TABLE `payments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `invoice_id` BIGINT NOT NULL,
    `payment_method` VARCHAR(30) NOT NULL, -- CASH, CARD, UPI
    `transaction_ref` VARCHAR(100),
    `payment_status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED', -- PENDING, COMPLETED, FAILED
    `paid_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pay_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `invoices` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 18. INVENTORY
CREATE TABLE `inventory` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `item_name` VARCHAR(100) NOT NULL UNIQUE,
    `unit` VARCHAR(20) NOT NULL, -- KG, LITRE, PACKET, PCS
    `current_stock` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `min_required_stock` DECIMAL(10, 2) NOT NULL DEFAULT 5.00,
    `cost_per_unit` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 19. STOCK HISTORY
CREATE TABLE `stock_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `inventory_id` BIGINT NOT NULL,
    `transaction_type` VARCHAR(20) NOT NULL, -- IN, OUT, ADJUSTMENT
    `quantity` DECIMAL(10, 2) NOT NULL,
    `notes` VARCHAR(255),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_sh_inventory` FOREIGN KEY (`inventory_id`) REFERENCES `inventory` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 20. NOTIFICATIONS
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `target_role` VARCHAR(30) NOT NULL, -- KITCHEN, WAITER, CASHIER, ADMIN, CUSTOMER
    `title` VARCHAR(100) NOT NULL,
    `message` TEXT NOT NULL,
    `order_id` BIGINT,
    `table_id` BIGINT,
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 21. AUDIT LOGS
CREATE TABLE `audit_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT,
    `username` VARCHAR(100),
    `action` VARCHAR(100) NOT NULL,
    `target_entity` VARCHAR(100),
    `details` TEXT,
    `ip_address` VARCHAR(50),
    `timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- INDEXES FOR HIGH PERFORMANCE QUERYING
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_table ON orders(table_id);
CREATE INDEX idx_menu_category ON menu_items(category_id);
CREATE INDEX idx_notifications_role ON notifications(target_role, is_read);
CREATE INDEX idx_inventory_stock ON inventory(current_stock);

-- -- SEED DATA (INDIAN RUPEES INR ₹ & 12 CATEGORIES)
INSERT INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'System Administrator with full access'),
(2, 'ROLE_WAITER', 'Floor Waiter managing tables and order delivery'),
(3, 'ROLE_KITCHEN', 'Chef and Kitchen Staff managing food preparation'),
(4, 'ROLE_CASHIER', 'Cashier managing billing, discounts, and payments');

INSERT INTO users (id, username, password, email, full_name, phone, role_id, status) VALUES
(1, 'admin', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'admin@cafemanagement.com', 'System Admin', '+1 555-0100', 1, 'ACTIVE'),
(2, 'waiter', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'waiter@cafemanagement.com', 'John Waiter', '+1 555-0101', 2, 'ACTIVE'),
(3, 'kitchen', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'kitchen@cafemanagement.com', 'Chef Gordon', '+1 555-0102', 3, 'ACTIVE'),
(4, 'cashier', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVym5F8N.2/iJ/s1N5123uMu', 'cashier@cafemanagement.com', 'Sarah Cashier', '+1 555-0103', 4, 'ACTIVE');

INSERT INTO restaurant_tables (id, table_number, capacity, status, qr_token, qr_code_url) VALUES
(1, 1, 2, 'AVAILABLE', 'TBL-QR-001', 'http://localhost:4200/customer/menu?table=1'),
(2, 2, 4, 'AVAILABLE', 'TBL-QR-002', 'http://localhost:4200/customer/menu?table=2'),
(3, 3, 4, 'AVAILABLE', 'TBL-QR-003', 'http://localhost:4200/customer/menu?table=3'),
(4, 4, 6, 'AVAILABLE', 'TBL-QR-004', 'http://localhost:4200/customer/menu?table=4'),
(5, 5, 2, 'AVAILABLE', 'TBL-QR-005', 'http://localhost:4200/customer/menu?table=5'),
(6, 6, 8, 'AVAILABLE', 'TBL-QR-006', 'http://localhost:4200/customer/menu?table=6');

INSERT INTO categories (id, name, description, image_url, display_order, active) VALUES
(1, 'Espresso & Coffee', 'Freshly roasted single-origin espresso and handcrafted coffee specialties', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80', 1, TRUE),
(2, 'Tea & Cold Drinks', 'Organic herbal teas, iced brews, fresh infusions, and matcha specialties', 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80', 2, TRUE),
(3, 'Gourmet Milkshakes', 'Thick decadent handcrafted milkshakes made with premium gelato and toppings', 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=600&q=80', 3, TRUE),
(4, 'Refreshing Mocktails', 'Exotic fruit infusions, sparkling botanical mocktails, and mint fizzers', 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80', 4, TRUE),
(5, '100% Fresh Fruit Juices', 'Raw cold-pressed fruit juices and nutrient-dense superfood elixirs', 'https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80', 5, TRUE),
(6, 'Gourmet Veg Pizzas', 'Hand-tossed wood-fired pizzas loaded with fresh garden vegetables and mozzarella', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80', 6, TRUE),
(7, 'Gourmet Non-Veg Pizzas', 'Artisanal stone-baked pizzas with spicy chicken, pepperoni, and succulent meats', 'https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=600&q=80', 7, TRUE),
(8, 'Artisan Burgers (Veg & Non-Veg)', 'Gourmet brioche burgers with paneer, crispy chicken, Angus beef and house sauces', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80', 8, TRUE),
(9, 'Authentic Shawarmas & Wraps', 'Fresh pita shawarma rolls, paneer kathi wraps, and falafel doners', 'https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&w=600&q=80', 9, TRUE),
(10, 'Artisanal Bakery & Toast', 'Freshly baked sourdough, butter croissants, focaccia, and gourmet toasts', 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80', 10, TRUE),
(11, 'Breakfast & Pancakes', 'Fluffy buttermilk pancakes, Eggs Benedict, French toast, and breakfast bowls', 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80', 11, TRUE),
(12, 'Desserts & Sweets', 'Decadent chocolate lava cake, New York cheesecake, and Italian desserts', 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80', 12, TRUE);

INSERT INTO menu_items (id, category_id, name, description, price, image_url, prep_time_mins, is_veg, is_available, is_featured) VALUES
(1, 1, 'Classic Double Espresso', 'Rich 100% Arabica double shot with velvety crema', 149.00, 'https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(2, 1, 'Caramel Cloud Cappuccino', 'Espresso with velvety steamed milk topped with salted caramel foam', 199.00, 'https://images.unsplash.com/photo-1572442388796-11668a67e53d?auto=format&fit=crop&w=600&q=80', 7, TRUE, TRUE, TRUE),
(3, 1, 'Iced Vanilla Bean Latte', 'Espresso over chilled oat milk with Madagascar vanilla bean syrup', 219.00, 'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, FALSE),
(4, 1, 'Spanish Sweet Cinnamon Latte', 'Double shot espresso infused with condensed milk and warm cinnamon notes', 209.00, 'https://images.unsplash.com/photo-1541167760496-1628856ab772?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, TRUE),
(5, 1, 'Hazelnut Dark Mocha', 'Rich Dutch dark chocolate melted with espresso and roasted hazelnut syrup', 229.00, 'https://images.unsplash.com/photo-1534778101976-62847782c213?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, FALSE),
(6, 1, 'Nitro Draft Cold Brew', '18-hour cold steeped coffee infused with nitrogen for a silky draft texture', 189.00, 'https://images.unsplash.com/photo-1461023058943-07fcbe16d735?auto=format&fit=crop&w=600&q=80', 4, TRUE, TRUE, TRUE),
(7, 1, 'Velvet Microfoam Flat White', 'Ristretto double shot mixed with velvety micro-foamed whole milk', 179.00, 'https://images.unsplash.com/photo-1577968897966-3d4325b36b61?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(8, 2, 'Japanese Iced Matcha Latte', 'Ceremonial grade Uji matcha whisked with almond milk and honey', 229.00, 'https://images.unsplash.com/photo-1536256263959-770b48d82b0a?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, TRUE),
(9, 2, 'Iced Peach & Passionfruit Tea', 'Brewed Black tea shaken with ripe peach nectar and passionfruit boba', 169.00, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(10, 2, 'Artisanal Masala Chai', 'Traditional Indian black tea simmered with cardamom, ginger, cloves and milk', 129.00, 'https://images.unsplash.com/photo-1576092768241-dec231879fc3?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, TRUE),
(11, 2, 'Earl Grey Lavender Tea', 'Bergamot scented tea infused with French lavender syrup and steamed milk', 189.00, 'https://images.unsplash.com/photo-1597481499750-3e6b22637e12?auto=format&fit=crop&w=600&q=80', 7, TRUE, TRUE, FALSE),
(12, 2, 'Hibiscus Berry Iced Cooler', 'Tart hibiscus flower infusion with mixed forest berries and crushed ice', 179.00, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(13, 2, 'Sparkling Cascara Tonic', 'Coffee cherry cascara infusion topped with tonic water and fresh orange slice', 189.00, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=600&q=80', 4, TRUE, TRUE, FALSE),
(14, 3, 'Belgian Dark Chocolate Shake', 'Rich Belgian cocoa blend with Madagascar vanilla gelato and dark chocolate shavings', 249.00, 'https://images.unsplash.com/photo-1572490122747-3968b75cc699?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, TRUE),
(15, 3, 'Nutella Hazelnut Freakshake', 'Decadent Nutella shake crowned with whipped cream, Ferrero Rocher and hazelnut drizzle', 289.00, 'https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?auto=format&fit=crop&w=600&q=80', 10, TRUE, TRUE, TRUE),
(16, 3, 'Salted Caramel Biscoff Shake', 'Lotus Biscoff spread blended with vanilla ice cream and salted caramel crumble', 269.00, 'https://images.unsplash.com/photo-1579954115545-a95591f28bfc?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, TRUE),
(17, 3, 'Strawberries & Cream Thickshake', 'Fresh handpicked strawberries blended with rich cream and strawberry coulis', 239.00, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80', 7, TRUE, TRUE, FALSE),
(18, 3, 'Oreo Cookie Supreme Shake', 'Crushed Oreo cookies blended with chocolate gelato and topped with chocolate fudge', 249.00, 'https://images.unsplash.com/photo-1553787499-6f9133860278?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, FALSE),
(19, 3, 'Alphonso Mango Thickshake', 'Pure Alphonso mango pulp blended with rich vanilla cream and crushed pistachios', 259.00, 'https://images.unsplash.com/photo-1546173159-315724a31696?auto=format&fit=crop&w=600&q=80', 7, TRUE, TRUE, TRUE),
(20, 4, 'Virgin Blue Lagoon Fizz', 'Blue Curacao syrup, fresh lime juice, mint leaves and sparkling Sprite', 219.00, 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(21, 4, 'Classic Mint Mojito Cooler', 'Muddled fresh garden mint, key lime chunks, brown sugar and club soda', 199.00, 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(22, 4, 'Passionfruit Mango Sparkler', 'Tropical passionfruit nectar, mango purée, lime and sparkling ginger ale', 229.00, 'https://images.unsplash.com/photo-1536935338788-846bb9981813?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, FALSE),
(23, 4, 'Watermelon Basil Splash', 'Fresh watermelon juice shaken with sweet Italian basil and lime', 209.00, 'https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(24, 4, 'Berry Blast Sangria Mocktail', 'Red grape juice, muddled raspberries, blueberries, orange slices and soda', 239.00, 'https://images.unsplash.com/photo-1621263764928-df1444c5e859?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, TRUE),
(25, 4, 'Cucumber Lime Botanist', 'Crisp cucumber ribbons, fresh lime juice, elderflower tonic and crushed ice', 199.00, 'https://images.unsplash.com/photo-1595981267035-7b04ca84a82d?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(26, 5, 'Cold-Pressed Valencia Orange', '100% pure cold-pressed Valencia oranges packed with natural Vitamin C', 189.00, 'https://images.unsplash.com/photo-1613478223719-2ab802602423?auto=format&fit=crop&w=600&q=80', 4, TRUE, TRUE, TRUE),
(27, 5, 'Fresh Watermelon Mint Juice', 'Hydrating fresh seedless watermelon juice pressed with garden mint', 169.00, 'https://images.unsplash.com/photo-1589733955941-5eeaf752f6dd?auto=format&fit=crop&w=600&q=80', 4, TRUE, TRUE, FALSE),
(28, 5, 'ABC Super Detox Juice', 'Nutrient-dense blend of Apple, Beetroot, Carrot and a hint of key lemon', 219.00, 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(29, 5, 'Pure Pineapple Ginger Elixir', 'Fresh golden pineapple juice infused with fresh ginger root and black salt', 179.00, 'https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(30, 5, 'Ruby Grapefruit & Honey Juice', 'Freshly squeezed pink grapefruit juice sweetened with wild forest honey', 199.00, 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?auto=format&fit=crop&w=600&q=80', 4, TRUE, TRUE, FALSE),
(31, 5, 'Green Goddess Immunity Juice', 'Cold-pressed Green Apple, Celery, Spinach, Cucumber and Ginger', 229.00, 'https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, TRUE),
(32, 6, 'Paneer Tikka Wood-Fired Pizza', 'Smoky tandoori paneer, red onion, green capsicum, and fresh mozzarella cheese', 389.00, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, TRUE),
(33, 6, 'Classic Margherita Supreme', 'Italian San Marzano tomato sauce, fresh basil leaves, extra virgin olive oil and mozzarella', 299.00, 'https://images.unsplash.com/photo-1604382354936-07c5d9983bd3?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, TRUE),
(34, 6, 'Farmhouse Veggie Supreme', 'Sweet corn, black olives, jalapenos, mushrooms, capsicum and melted cheddar cheese', 369.00, 'https://images.unsplash.com/photo-1571407970349-bc81e7e96d47?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, FALSE),
(35, 6, 'Truffle Wild Mushroom Pizza', 'Sauteed wild portobello mushrooms, garlic cream sauce, truffle drizzle and mozzarella', 449.00, 'https://images.unsplash.com/photo-1590947132387-155cc02f3212?auto=format&fit=crop&w=600&q=80', 16, TRUE, TRUE, TRUE),
(36, 6, '4-Cheese Veggie Burst Pizza', 'Rich blend of Mozzarella, Processed Cheddar, Gouda and Cream Cheese crust', 429.00, 'https://images.unsplash.com/photo-1574071318508-1cdbab80d002?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, FALSE),
(37, 7, 'Spicy Chicken Pepperoni Pizza', 'Smoky chicken pepperoni slices, chili flakes, and double mozzarella cheese', 449.00, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?auto=format&fit=crop&w=600&q=80', 16, FALSE, TRUE, TRUE),
(38, 7, 'BBQ Smoked Chicken Pizza', 'Slow-cooked BBQ shredded chicken, caramelized red onions, coriander and mozzarella', 469.00, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?auto=format&fit=crop&w=600&q=80', 15, FALSE, TRUE, TRUE),
(39, 7, 'Butter Chicken Tikka Feast Pizza', 'Juicy makhani chicken chunks, red paprika, capsicum, and melted mozzarella cheese', 479.00, 'https://images.unsplash.com/photo-1534308983496-4fabb1a015ee?auto=format&fit=crop&w=600&q=80', 16, FALSE, TRUE, TRUE),
(40, 7, 'Fiery Lamb & Mutton Supreme Pizza', 'Spicy minced mutton keema, jalapenos, red onions and sharp cheddar cheese', 549.00, 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?auto=format&fit=crop&w=600&q=80', 18, FALSE, TRUE, FALSE),
(41, 7, 'Ultimate Meat Lovers Pizza', 'Loaded with grilled chicken sausage, chicken pepperoni, smoked bacon and ham', 529.00, 'https://images.unsplash.com/photo-1544982503-9f984c14501a?auto=format&fit=crop&w=600&q=80', 18, FALSE, TRUE, TRUE),
(42, 8, 'Crispy Paneer Crunch Burger', 'Crispy spiced paneer patty, thousand island sauce, lettuce & tomato on brioche', 199.00, 'https://images.unsplash.com/photo-1550547660-d9450f859349?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, TRUE),
(43, 8, 'Veggie Deluxe Bean Burger', 'House black bean & corn patty, avocado relish, chipotle mayo and cheddar cheese', 179.00, 'https://images.unsplash.com/photo-1520072959219-c595dc870360?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, FALSE),
(44, 8, 'Crispy Spicy Chicken Brioche Burger', 'Fried buttermilk chicken breast, jalapeno slaw, and spicy mayo on toasted brioche', 259.00, 'https://images.unsplash.com/photo-1625813506062-0aeb1d7a094b?auto=format&fit=crop&w=600&q=80', 15, FALSE, TRUE, TRUE),
(45, 8, 'Double Cheese Angus Beef Burger', '100% Angus beef patty, double melted cheddar, caramelized onions and pickles', 329.00, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80', 18, FALSE, TRUE, TRUE),
(46, 8, 'Smoky Lamb Mushroom Swiss Burger', 'Juicy grilled lamb patty, sauteed portobello mushrooms & Swiss cheese', 299.00, 'https://images.unsplash.com/photo-1586190848861-99aa4a171e90?auto=format&fit=crop&w=600&q=80', 16, FALSE, TRUE, FALSE),
(47, 9, 'Classic Chicken Pita Shawarma', 'Slow-roasted chicken garlic shawarma, garlic toum, pickles & fries in soft pita', 199.00, 'https://images.unsplash.com/photo-1561651823-34feb02250e4?auto=format&fit=crop&w=600&q=80', 10, FALSE, TRUE, TRUE),
(48, 9, 'Paneer Tikka Kathi Wrap', 'Char-grilled cottage cheese cubes, mint chutney, sliced onions in rumali roti', 169.00, 'https://images.unsplash.com/photo-1626777552726-4a6b54c97e46?auto=format&fit=crop&w=600&q=80', 10, TRUE, TRUE, TRUE),
(49, 9, 'Crispy Falafel Hummus Veggie Wrap', 'Golden chickpea falafel, tahini, creamy hummus, tabbouleh and pickled veggies', 159.00, 'https://images.unsplash.com/photo-1541518763669-27fef04b14da?auto=format&fit=crop&w=600&q=80', 10, TRUE, TRUE, FALSE),
(50, 9, 'Special Lamb Doner Shawarma', 'Spiced roasted lamb slices, sumac onions, garlic sauce and fresh parsley wrap', 269.00, 'https://images.unsplash.com/photo-1529006557810-274b9b2fc783?auto=format&fit=crop&w=600&q=80', 12, FALSE, TRUE, TRUE),
(51, 9, 'Spicy Peri-Peri Chicken Roll', 'Juicy chicken tikka tossed in spicy peri-peri marinade wrapped with cheese', 219.00, 'https://images.unsplash.com/photo-1606755962773-d324e0a13086?auto=format&fit=crop&w=600&q=80', 10, FALSE, TRUE, TRUE),
(52, 10, 'Sourdough Avocado Toast', 'Smashed Hass avocado, poached egg, cherry tomatoes & feta crumble on sourdough', 249.00, 'https://images.unsplash.com/photo-1588137378633-dea1336ce1e2?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, TRUE),
(53, 10, 'French Butter Croissant', 'Flaky French butter croissant baked fresh every morning', 129.00, 'https://images.unsplash.com/photo-1555507036-ab1f4038808a?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, FALSE),
(54, 10, 'Almond Frangipane Danish', 'Golden puff pastry filled with almond cream and toasted almond flakes', 159.00, 'https://images.unsplash.com/photo-1608198093002-ad4e005484ec?auto=format&fit=crop&w=600&q=80', 5, TRUE, TRUE, TRUE),
(55, 10, 'Rosemary Garlic Herb Focaccia', 'Warm sea salt focaccia drizzled with roasted garlic and extra virgin olive oil', 179.00, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, FALSE),
(56, 11, 'Blueberry Maple Pancake Stack', 'Fluffy triple-stacked pancakes with wild blueberry compote and warm maple syrup', 299.00, 'https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=600&q=80', 15, TRUE, TRUE, TRUE),
(57, 11, 'Classic Eggs Benedict', 'Poached eggs, smoked turkey ham, Hollandaise sauce on toasted English muffin', 329.00, 'https://images.unsplash.com/photo-1608039829572-78524f79c4c7?auto=format&fit=crop&w=600&q=80', 14, FALSE, TRUE, TRUE),
(58, 11, 'Brioche Vanilla French Toast', 'Thick brioche slice dipped in cinnamon egg batter with berries and maple syrup', 279.00, 'https://images.unsplash.com/photo-1525351484163-7529414344d8?auto=format&fit=crop&w=600&q=80', 12, TRUE, TRUE, FALSE),
(59, 12, 'Belgian Chocolate Lava Cake', 'Warm molten chocolate cake served with Madagascar vanilla gelato', 249.00, 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?auto=format&fit=crop&w=600&q=80', 10, TRUE, TRUE, TRUE),
(60, 12, 'New York Baked Cheesecake', 'Dense and creamy classic NY cheesecake with wild berry compote', 229.00, 'https://images.unsplash.com/photo-1533134242443-d4fd215305ad?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, TRUE),
(61, 12, 'Classic Italian Tiramisu Cup', 'Espresso-soaked ladyfingers layered with mascarpone cream and cocoa powder', 219.00, 'https://images.unsplash.com/photo-1571877227200-a0d98ea607e9?auto=format&fit=crop&w=600&q=80', 8, TRUE, TRUE, FALSE),
(62, 12, 'Sicilian Pistachio Cannoli', 'Crispy pastry shell stuffed with sweet ricotta cream and crushed Bronte pistachios', 199.00, 'https://images.unsplash.com/photo-1551024709-8f23befc6f87?auto=format&fit=crop&w=600&q=80', 6, TRUE, TRUE, TRUE);

INSERT INTO coupons (id, code, description, discount_type, discount_value, min_order_amount, max_discount, valid_until, active) VALUES
(1, 'WELCOME10', '10% discount on orders over ₹300', 'PERCENTAGE', 10.00, 300.00, 150.00, '2027-12-31', TRUE),
(2, 'FLAT50', 'Flat ₹50 off on orders over ₹500', 'FLAT', 50.00, 500.00, 50.00, '2027-12-31', TRUE);

INSERT INTO inventory (id, item_name, unit, current_stock, min_required_stock, cost_per_unit) VALUES
(1, 'Single Origin Arabica Beans', 'KG', 25.50, 5.00, 800.00),
(2, 'Whole Organic Milk', 'LITRE', 40.00, 10.00, 60.00),
(3, 'Artisanal Sourdough Loaf', 'PACKET', 12.00, 4.00, 90.00),
(4, 'Fresh Paneer Cubes', 'KG', 20.00, 5.00, 320.00),
(5, 'Boneless Chicken Breast', 'KG', 30.00, 8.00, 260.00),
(6, 'Madagascar Vanilla Gelato', 'KG', 15.00, 3.00, 450.00);
