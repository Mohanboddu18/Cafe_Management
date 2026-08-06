import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { KitchenService } from '../../core/services/kitchen.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { ToastService } from '../../core/services/toast.service';
import { Order } from '../../core/models/cafe.models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-kitchen-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-fluid py-4 px-4">
      <div class="d-flex align-items-center justify-content-between mb-4 bg-dark text-white p-3 rounded-4 shadow-sm">
        <div class="d-flex align-items-center gap-3">
          <div class="bg-warning text-dark rounded-circle p-3 d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
            <i class="fa-solid fa-fire-burner fs-3"></i>
          </div>
          <div>
            <h3 class="font-serif fw-bold mb-0">Live Kitchen Display System (KDS)</h3>
            <span class="text-white-50 small"><i class="fa-solid fa-circle text-success me-1"></i> Real-Time WebSocket Connected</span>
          </div>
        </div>
        <button class="btn btn-outline-light rounded-pill btn-sm px-3" (click)="loadOrders()">
          <i class="fa-solid fa-rotate me-1"></i> Refresh Orders
        </button>
      </div>

      <!-- Live Order Columns -->
      <div class="row g-4">
        <!-- Column 1: NEW ORDERS -->
        <div class="col-md-4">
          <div class="bg-light rounded-4 p-3 border shadow-sm h-100">
            <div class="d-flex align-items-center justify-content-between mb-3 border-bottom pb-2">
              <h5 class="fw-bold mb-0 text-primary"><i class="fa-solid fa-bell me-2"></i> New Orders</h5>
              <span class="badge bg-primary rounded-pill fs-6">{{ newOrders.length }}</span>
            </div>

            <div *ngIf="!newOrders.length" class="text-center text-muted py-5">
              <i class="fa-solid fa-check-double display-4 mb-2 opacity-50"></i>
              <p>No new orders pending</p>
            </div>

            <div *ngFor="let order of newOrders" class="card border-0 shadow-sm rounded-4 mb-3 p-3 border-start border-4 border-primary">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge bg-dark font-monospace fs-6">TABLE #{{ order.tableNumber }}</span>
                <span class="text-muted small"><i class="fa-regular fa-clock me-1"></i>{{ order.orderTime }}</span>
              </div>
              <h6 class="fw-bold mb-1">Order #{{ order.orderNumber }}</h6>
              <div class="text-muted small mb-2" *ngIf="order.notes"><em>Note: {{ order.notes }}</em></div>

              <div class="bg-white rounded-3 p-2 mb-3 border">
                <div *ngFor="let item of order.items" class="d-flex justify-content-between small py-1 border-bottom last-border-0">
                  <span class="fw-bold">{{ item.quantity }}x {{ item.menuItemName }}</span>
                  <span class="text-muted">{{ item.notes }}</span>
                </div>
              </div>

              <button class="btn btn-primary w-100 rounded-pill fw-bold" (click)="updateStatus(order.id, 'PREPARING')">
                <i class="fa-solid fa-kitchen-set me-1"></i> Start Cooking
              </button>
            </div>
          </div>
        </div>

        <!-- Column 2: IN PREPARATION -->
        <div class="col-md-4">
          <div class="bg-light rounded-4 p-3 border shadow-sm h-100">
            <div class="d-flex align-items-center justify-content-between mb-3 border-bottom pb-2">
              <h5 class="fw-bold mb-0 text-warning"><i class="fa-solid fa-fire me-2"></i> Cooking in Progress</h5>
              <span class="badge bg-warning text-dark rounded-pill fs-6">{{ preparingOrders.length }}</span>
            </div>

            <div *ngIf="!preparingOrders.length" class="text-center text-muted py-5">
              <i class="fa-solid fa-kitchen-set display-4 mb-2 opacity-50"></i>
              <p>No dishes currently cooking</p>
            </div>

            <div *ngFor="let order of preparingOrders" class="card border-0 shadow-sm rounded-4 mb-3 p-3 border-start border-4 border-warning">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge bg-dark font-monospace fs-6">TABLE #{{ order.tableNumber }}</span>
                <span class="badge bg-warning text-dark"><i class="fa-regular fa-clock me-1"></i>Cooking</span>
              </div>
              <h6 class="fw-bold mb-1">Order #{{ order.orderNumber }}</h6>

              <div class="bg-white rounded-3 p-2 mb-3 border">
                <div *ngFor="let item of order.items" class="d-flex justify-content-between small py-1 border-bottom last-border-0">
                  <span class="fw-bold">{{ item.quantity }}x {{ item.menuItemName }}</span>
                  <span class="text-muted">{{ item.notes }}</span>
                </div>
              </div>

              <button class="btn btn-success w-100 rounded-pill fw-bold" (click)="updateStatus(order.id, 'READY')">
                <i class="fa-solid fa-circle-check me-1"></i> Mark as READY
              </button>
            </div>
          </div>
        </div>

        <!-- Column 3: READY FOR SERVING -->
        <div class="col-md-4">
          <div class="bg-light rounded-4 p-3 border shadow-sm h-100">
            <div class="d-flex align-items-center justify-content-between mb-3 border-bottom pb-2">
              <h5 class="fw-bold mb-0 text-success"><i class="fa-solid fa-circle-check me-2"></i> Ready to Serve</h5>
              <span class="badge bg-success rounded-pill fs-6">{{ readyOrders.length }}</span>
            </div>

            <div *ngIf="!readyOrders.length" class="text-center text-muted py-5">
              <i class="fa-solid fa-drumstick-bite display-4 mb-2 opacity-50"></i>
              <p>No orders waiting for pickup</p>
            </div>

            <div *ngFor="let order of readyOrders" class="card border-0 shadow-sm rounded-4 mb-3 p-3 border-start border-4 border-success">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="badge bg-dark font-monospace fs-6">TABLE #{{ order.tableNumber }}</span>
                <span class="badge bg-success"><i class="fa-solid fa-bell me-1"></i>Waiter Notified</span>
              </div>
              <h6 class="fw-bold mb-1">Order #{{ order.orderNumber }}</h6>

              <div class="bg-white rounded-3 p-2 mb-2 border">
                <div *ngFor="let item of order.items" class="small py-1">
                  <span class="fw-bold">{{ item.quantity }}x {{ item.menuItemName }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class KitchenDashboardComponent implements OnInit, OnDestroy {
  orders: Order[] = [];
  private pollSub!: Subscription;
  private wsSub!: Subscription;

  constructor(
    private kitchenService: KitchenService,
    private wsService: WebSocketService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadOrders();
    this.pollSub = interval(5000).subscribe(() => this.loadOrders());

    this.wsSub = this.wsService.notification$.subscribe(notif => {
      if (notif && (notif.targetRole === 'KITCHEN' || notif.targetRole === 'ALL')) {
        this.toastService.show(`[KITCHEN ALERT] ${notif.title}: ${notif.message}`, 'warning');
        this.loadOrders();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pollSub) this.pollSub.unsubscribe();
    if (this.wsSub) this.wsSub.unsubscribe();
  }

  loadOrders(): void {
    this.kitchenService.getLiveOrders().subscribe(res => this.orders = res);
  }

  get newOrders(): Order[] {
    return this.orders.filter(o => o.status === 'NEW' || o.status === 'ACCEPTED');
  }

  get preparingOrders(): Order[] {
    return this.orders.filter(o => o.status === 'PREPARING');
  }

  get readyOrders(): Order[] {
    return this.orders.filter(o => o.status === 'READY');
  }

  updateStatus(orderId: number, status: string): void {
    this.kitchenService.updateOrderStatus(orderId, status).subscribe({
      next: () => {
        this.toastService.show(`Order status updated to ${status}`, 'success');
        this.loadOrders();
      }
    });
  }
}
