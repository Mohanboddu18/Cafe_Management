import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { WaiterService } from '../../core/services/waiter.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { ToastService } from '../../core/services/toast.service';
import { RestaurantTable, Order } from '../../core/models/cafe.models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-waiter-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-fluid py-4 px-4">
      <!-- Header -->
      <div class="d-flex align-items-center justify-content-between mb-4 bg-dark text-white p-3 rounded-4 shadow-sm">
        <div class="d-flex align-items-center gap-3">
          <div class="bg-warning text-dark rounded-circle p-3 d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
            <i class="fa-solid fa-chair fs-3"></i>
          </div>
          <div>
            <h3 class="font-serif fw-bold mb-0">Waiter Floor Control</h3>
            <span class="text-white-50 small">Manage Table Statuses & Deliver Ready Food</span>
          </div>
        </div>
        <button class="btn btn-warning text-dark fw-bold rounded-pill" (click)="showWalkInModal = true">
          <i class="fa-solid fa-plus me-1"></i> Create Walk-in Order
        </button>
      </div>

      <!-- Ready Food Alert Banner -->
      <div *ngIf="readyOrders.length" class="card border-0 bg-success text-white shadow-sm rounded-4 mb-4 p-3">
        <div class="d-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center gap-3">
            <i class="fa-solid fa-bell fs-2 pulse-alert"></i>
            <div>
              <h5 class="fw-bold mb-0">{{ readyOrders.length }} Order(s) READY in Kitchen!</h5>
              <div class="small">Deliver food to respective tables immediately</div>
            </div>
          </div>
        </div>
        <div class="row g-3 mt-2">
          <div class="col-md-4" *ngFor="let ro of readyOrders">
            <div class="bg-white text-dark rounded-3 p-3 shadow-sm">
              <div class="d-flex justify-content-between fw-bold mb-1">
                <span>TABLE #{{ ro.tableNumber }}</span>
                <span>#{{ ro.orderNumber }}</span>
              </div>
              <div class="small text-muted mb-2">Items: {{ ro.items.length }} dishes</div>
              <button class="btn btn-success btn-sm w-100 rounded-pill fw-bold" (click)="markServed(ro.id)">
                <i class="fa-solid fa-check me-1"></i> Deliver Food (Served)
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Restaurant Tables Matrix Grid -->
      <h4 class="font-serif fw-bold mb-3">Table Status Overview</h4>
      <div class="row g-4 mb-5">
        <div class="col-6 col-md-4 col-lg-3" *ngFor="let table of tables">
          <div class="glass-card p-4 text-center h-100 border-start border-4"
               [ngClass]="{
                 'border-success': table.status === 'AVAILABLE',
                 'border-danger': table.status === 'OCCUPIED',
                 'border-warning': table.status === 'BILL_REQUESTED'
               }">
            <div class="d-flex justify-content-between align-items-center mb-2">
              <span class="badge bg-dark font-monospace">CAP: {{ table.capacity }}</span>
              <span class="badge rounded-pill"
                    [ngClass]="{
                      'bg-success': table.status === 'AVAILABLE',
                      'bg-danger': table.status === 'OCCUPIED',
                      'bg-warning text-dark': table.status === 'BILL_REQUESTED'
                    }">
                {{ table.status }}
              </span>
            </div>

            <div class="my-3">
              <i class="fa-solid fa-chair display-4"
                 [ngClass]="{
                   'text-success': table.status === 'AVAILABLE',
                   'text-danger': table.status === 'OCCUPIED',
                   'text-warning': table.status === 'BILL_REQUESTED'
                 }"></i>
              <h3 class="font-serif fw-bold mt-2 mb-0">Table {{ table.tableNumber }}</h3>
            </div>

            <div class="dropdown mt-3">
              <button class="btn btn-outline-dark btn-sm rounded-pill dropdown-toggle w-100" type="button" data-bs-toggle="dropdown">
                Change Status
              </button>
              <ul class="dropdown-menu">
                <li><a class="dropdown-item text-success" (click)="updateTableStatus(table.id, 'AVAILABLE')">AVAILABLE</a></li>
                <li><a class="dropdown-item text-danger" (click)="updateTableStatus(table.id, 'OCCUPIED')">OCCUPIED</a></li>
                <li><a class="dropdown-item text-warning" (click)="updateTableStatus(table.id, 'BILL_REQUESTED')">BILL REQUESTED</a></li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class WaiterDashboardComponent implements OnInit, OnDestroy {
  tables: RestaurantTable[] = [];
  readyOrders: Order[] = [];
  showWalkInModal = false;
  private pollSub!: Subscription;
  private wsSub!: Subscription;

  constructor(
    private waiterService: WaiterService,
    private wsService: WebSocketService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.pollSub = interval(5000).subscribe(() => this.loadData());

    this.wsSub = this.wsService.notification$.subscribe(notif => {
      if (notif && (notif.targetRole === 'WAITER' || notif.targetRole === 'ALL')) {
        this.toastService.show(`[WAITER ALERT] ${notif.title}: ${notif.message}`, 'warning');
        this.loadData();
      }
    });
  }

  ngOnDestroy(): void {
    if (this.pollSub) this.pollSub.unsubscribe();
    if (this.wsSub) this.wsSub.unsubscribe();
  }

  loadData(): void {
    this.waiterService.getAllTables().subscribe(res => this.tables = res);
    this.waiterService.getReadyOrders().subscribe(res => this.readyOrders = res);
  }

  updateTableStatus(tableId: number, status: string): void {
    this.waiterService.updateTableStatus(tableId, status).subscribe({
      next: () => {
        this.toastService.show(`Table status updated to ${status}`, 'success');
        this.loadData();
      }
    });
  }

  markServed(orderId: number): void {
    this.waiterService.markServed(orderId).subscribe({
      next: () => {
        this.toastService.show('Order marked as SERVED!', 'success');
        this.loadData();
      }
    });
  }
}
