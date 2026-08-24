# Frontend Architecture & Engineering Specifications (Angular 20 Standalone)

This document details the frontend architecture, reactive component design, routing, state management, and custom CSS design system powering the **Cafe Management System**.

---

## 🅰️ Angular 20 Architecture Overview

The frontend application uses **Angular 20 Standalone Components** without `NgModule` boilerplate, providing clean dependency injection, rapid rendering performance, and clear feature boundaries.

```
src/app/
├── app.component.ts                   # Root Component & Layout Shell
├── app.routes.ts                      # Main Application Routing Table & Role Guards
│
├── core/                              # Core Singleton Business Infrastructure
│   ├── guards/                        # Route Protection Guards
│   │   ├── auth.guard.ts              # Protects staff routes from unauthenticated access
│   │   └── role.guard.ts              # Protects specific routes by staff role (ADMIN, KITCHEN, etc.)
│   ├── interceptors/                  # Angular HTTP Interceptors
│   │   └── jwt.interceptor.ts         # Attaches 'Authorization: Bearer <token>' to API requests
│   ├── models/                        # Domain Interfaces & Models
│   │   ├── auth.model.ts
│   │   ├── menu.model.ts
│   │   ├── order.model.ts
│   │   ├── table.model.ts
│   │   ├── invoice.model.ts
│   │   ├── inventory.model.ts
│   │   └── review.model.ts
│   └── services/                      # Injectable RxJS HTTP & WebSocket Services
│       ├── auth.service.ts            # Authentication state, login & JWT token storage
│       ├── cart.service.ts            # Customer cart state management (BehaviorSubject)
│       ├── menu.service.ts            # Category & menu fetching service
│       ├── order.service.ts           # Order submission & customer order tracking service
│       ├── websocket.service.ts       # STOMP WebSocket connection & topic subscriptions
│       ├── kitchen.service.ts         # KDS order status transitions & audio chimes
│       ├── waiter.service.ts          # Waiter floor table matrix & pickup alerts
│       ├── billing.service.ts         # Cashier billing, coupon discount application & PDF download
│       ├── admin.service.ts           # Admin analytics, inventory CRUD, QR code generation
│       └── review.service.ts          # Customer rating submission service
│
├── features/                          # Feature Pages & User Dashboards
│   ├── admin/                         # Admin Management Suite
│   │   ├── admin-dashboard.component.ts    # Central Admin Portal & Quick Metrics
│   │   ├── menu-management.component.ts    # Category & Menu Item CRUD with Image upload
│   │   ├── table-management.component.ts   # Table configuration & ZXing QR generator
│   │   ├── inventory-management.component.ts# Ingredient stock management & low-stock alerts
│   │   ├── employee-management.component.ts # Staff user account creation & role assignment
│   │   └── analytics.component.ts          # Daily/Weekly revenue charts & Excel exports
│   ├── auth/
│   │   └── login.component.ts         # Staff Login Form Component
│   ├── cashier/
│   │   ├── billing.component.ts       # Active orders table, coupon input & tax computation
│   │   └── invoice.component.ts       # Print preview modal & PDF download component
│   ├── customer/
│   │   ├── menu.component.ts          # Customer Digital Menu with category tabs & search
│   │   ├── cart.component.ts          # Floating Cart Drawer with item customization & notes
│   │   ├── order-status.component.ts  # Live WebSocket Order Tracking Progress Page
│   │   └── review-dialog.component.ts # Customer feedback & star rating submission
│   ├── kitchen/
│   │   └── kds-dashboard.component.ts # Real-Time KDS Kanban Board (NEW, PREPARING, READY)
│   └── waiter/
│       └── waiter-dashboard.component.ts# Interactive Table Grid, Walk-In Order & Pickup Alerts
│
└── shared/                            # Reusable UI Shared Components
    ├── navbar/                        # Global Navigation Bar with active role indicator
    ├── sidebar/                       # Admin & Staff Sidebar Navigation
    ├── modal/                         # Reusable Glassmorphic Popup Modal
    └── toast/                         # Global Notification Toast System
```

---

## 🛠️ Reactive State Management (RxJS)

State management across feature modules is driven by RxJS `BehaviorSubject` streams, ensuring instant reactive UI updates without external state libraries.

### 1. Customer Shopping Cart (`CartService.ts`)
- Holds `cartItems$` BehaviorSubject stream.
- Exposes computed observables:
  - `totalCount$`: Total item count badge displayed on cart icon.
  - `subtotal$`: Aggregated subtotal price of selected items.
- Persists shopping cart state in `sessionStorage` so customer table orders survive page refreshes.

### 2. Real-Time KDS Order Queue (`KitchenService.ts`)
- Subscribes to `WebSocketService.subscribe('/topic/kitchen-orders')`.
- Maintains active orders array in memory.
- Plays Web Audio synthesized chime when a new order payload arrives on WebSocket channel.
- Moves order cards seamlessly across Kanban columns (`NEW` $\rightarrow$ `PREPARING` $\rightarrow$ `READY`).

---

## 🎨 Styling System & Design System (`styles.css`)

The application features a modern dark-mode glassmorphism aesthetic built entirely using **Vanilla CSS Custom Properties**.

### CSS Design Tokens & Variable System
```css
:root {
  /* Primary & Accent Palette */
  --primary-color: #ff6b35;
  --primary-hover: #ff8555;
  --accent-color: #004e64;
  --accent-glow: rgba(0, 78, 100, 0.4);

  /* Dark Theme Surface Colors */
  --bg-dark: #0f172a;
  --bg-card: rgba(30, 41, 59, 0.7);
  --bg-card-hover: rgba(51, 65, 85, 0.8);
  --border-glass: rgba(255, 255, 255, 0.1);

  /* Status Colors */
  --status-new: #3b82f6;
  --status-preparing: #f59e0b;
  --status-ready: #10b981;
  --status-served: #6b7280;

  /* Typography & Shadows */
  --font-family: 'Inter', system-ui, -apple-system, sans-serif;
  --glass-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
  --radius-lg: 16px;
  --radius-md: 10px;
}
```

### Key Visual Effects
- **Glassmorphism**: `backdrop-filter: blur(12px)` with subtle border highlights (`border: 1px solid var(--border-glass)`).
- **Responsive Layout**: Fluid CSS Grid (`grid-template-columns: repeat(auto-fill, minmax(280px, 1fr))`) adapt seamlessly from mobile devices to large kitchen display monitors.
- **Interactive Animations**: Micro-transitions (`transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1)`), hover elevation, and pulsing status badges.

---

## 🗺️ Application Routing Table (`app.routes.ts`)

```typescript
export const routes: Routes = [
  // Customer Routes (No Login Required)
  { path: 'customer/menu', component: MenuComponent },
  { path: 'customer/order-status/:id', component: OrderStatusComponent },

  // Staff Authentication Route
  { path: 'login', component: LoginComponent },

  // Staff Protected Routes
  { 
    path: 'kitchen/dashboard', 
    component: KdsDashboardComponent, 
    canActivate: [AuthGuard, RoleGuard], 
    data: { roles: ['ROLE_KITCHEN', 'ROLE_ADMIN'] } 
  },
  { 
    path: 'waiter/dashboard', 
    component: WaiterDashboardComponent, 
    canActivate: [AuthGuard, RoleGuard], 
    data: { roles: ['ROLE_WAITER', 'ROLE_ADMIN'] } 
  },
  { 
    path: 'cashier/billing', 
    component: BillingComponent, 
    canActivate: [AuthGuard, RoleGuard], 
    data: { roles: ['ROLE_CASHIER', 'ROLE_ADMIN'] } 
  },
  { 
    path: 'admin/dashboard', 
    component: AdminDashboardComponent, 
    canActivate: [AuthGuard, RoleGuard], 
    data: { roles: ['ROLE_ADMIN'] } 
  },

  // Fallback Route
  { path: '**', redirectTo: 'customer/menu?table=1' }
];
```
