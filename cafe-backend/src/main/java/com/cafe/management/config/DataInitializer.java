package com.cafe.management.config;

import com.cafe.management.entity.Role;
import com.cafe.management.entity.User;
import com.cafe.management.repository.RoleRepository;
import com.cafe.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.cafe.management.repository.NotificationRepository notificationRepository;

    @Autowired
    private com.cafe.management.repository.RestaurantTableRepository tableRepository;

    @Autowired
    private com.cafe.management.repository.OrderRepository orderRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(">>> [DataInitializer] Initializing staff user password hashes...");

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
