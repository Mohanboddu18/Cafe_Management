import { Routes } from '@angular/router';
import { TableSelectionComponent } from './features/customer/table-selection.component';
import { MenuComponent } from './features/customer/menu.component';
import { OrderTrackingComponent } from './features/customer/order-tracking.component';
import { LoginComponent } from './features/auth/login.component';
import { KitchenDashboardComponent } from './features/kitchen/kitchen-dashboard.component';
import { WaiterDashboardComponent } from './features/waiter/waiter-dashboard.component';
import { CashierBillingComponent } from './features/cashier/cashier-billing.component';
import { AdminDashboardComponent } from './features/admin/admin-dashboard.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'customer/tables', pathMatch: 'full' },
  { path: 'customer/tables', component: TableSelectionComponent, title: 'Select Table | Artisanal Cafe' },
  { path: 'customer/menu', component: MenuComponent, title: 'Digital Menu | Artisanal Cafe' },
  { path: 'customer/tracking/:id', component: OrderTrackingComponent, title: 'Order Tracking & Bill | Artisanal Cafe' },
  { path: 'login', component: LoginComponent, title: 'Staff Login | Artisanal Cafe' },
  { path: 'kitchen/dashboard', component: KitchenDashboardComponent, title: 'Kitchen Board | Artisanal Cafe', canActivate: [authGuard], data: { role: 'ROLE_KITCHEN' } },
  { path: 'waiter/dashboard', component: WaiterDashboardComponent, title: 'Active Tables | Artisanal Cafe', canActivate: [authGuard], data: { role: 'ROLE_WAITER' } },
  { path: 'cashier/billing', component: CashierBillingComponent, title: 'Cashier Billing | Artisanal Cafe', canActivate: [authGuard], data: { role: 'ROLE_CASHIER' } },
  { path: 'admin/dashboard', component: AdminDashboardComponent, title: 'Admin Portal | Artisanal Cafe', canActivate: [authGuard], data: { role: 'ROLE_ADMIN' } },
  { path: '**', redirectTo: 'customer/tables' }
];
