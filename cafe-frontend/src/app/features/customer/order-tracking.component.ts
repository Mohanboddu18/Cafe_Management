import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { ToastService } from '../../core/services/toast.service';
import { Order } from '../../core/models/cafe.models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-order-tracking',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container py-5" *ngIf="order">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="glass-card p-4 p-md-5 mb-4">
            <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
              <div>
                <span class="badge bg-warning text-dark font-monospace mb-1">TABLE #{{ order.tableNumber }}</span>
                <h2 class="font-serif fw-bold mb-0">Order #{{ order.orderNumber }}</h2>
                <div class="text-muted small">Placed at {{ order.orderTime }}</div>
              </div>
              <div class="text-end">
                <span class="badge fs-6 px-3 py-2" [ngClass]="getStatusBadgeClass(order.status)">
                  {{ order.status }}
                </span>
              </div>
            </div>

            <!-- Stepper Timeline Progress Bar -->
            <div class="my-5 position-relative">
              <div class="progress" style="height: 6px;">
                <div class="progress-bar bg-warning progress-bar-striped progress-bar-animated" 
                     role="progressbar" 
                     [style.width.%]="getProgressPercent(order.status)"></div>
              </div>
              <div class="d-flex justify-content-between position-relative mt-n3">
                <div class="text-center" [ngClass]="{ 'fw-bold text-dark': isStepPassed('NEW') }">
                  <div class="step-circle mx-auto mb-2 rounded-circle d-flex align-items-center justify-content-center"
                       [ngClass]="isStepPassed('NEW') ? 'bg-warning text-dark' : 'bg-secondary text-white'">
                    <i class="fa-solid fa-receipt"></i>
                  </div>
                  <span class="small d-block">Received</span>
                </div>
                <div class="text-center" [ngClass]="{ 'fw-bold text-dark': isStepPassed('PREPARING') }">
                  <div class="step-circle mx-auto mb-2 rounded-circle d-flex align-items-center justify-content-center"
                       [ngClass]="isStepPassed('PREPARING') ? 'bg-warning text-dark' : 'bg-secondary text-white'">
                    <i class="fa-solid fa-fire-burner"></i>
                  </div>
                  <span class="small d-block">Cooking</span>
                </div>
                <div class="text-center" [ngClass]="{ 'fw-bold text-dark': isStepPassed('READY') }">
                  <div class="step-circle mx-auto mb-2 rounded-circle d-flex align-items-center justify-content-center"
                       [ngClass]="isStepPassed('READY') ? 'bg-warning text-dark' : 'bg-secondary text-white'">
                    <i class="fa-solid fa-bell"></i>
                  </div>
                  <span class="small d-block">Ready</span>
                </div>
                <div class="text-center" [ngClass]="{ 'fw-bold text-dark': isStepPassed('SERVED') }">
                  <div class="step-circle mx-auto mb-2 rounded-circle d-flex align-items-center justify-content-center"
                       [ngClass]="isStepPassed('SERVED') ? 'bg-warning text-dark' : 'bg-secondary text-white'">
                    <i class="fa-solid fa-utensils"></i>
                  </div>
                  <span class="small d-block">Served</span>
                </div>
              </div>
            </div>

            <!-- Ordered Items List -->
            <h5 class="fw-bold mb-3 font-serif">Order Summary</h5>
            <div class="card border-0 bg-light rounded-4 p-3 mb-4">
              <div *ngFor="let item of order.items" class="d-flex align-items-center justify-content-between py-2 border-bottom last-border-0">
                <div class="d-flex align-items-center gap-3">
                  <span class="badge bg-secondary rounded-pill px-3">{{ item.quantity }}x</span>
                  <div>
                    <div class="fw-bold text-dark">{{ item.menuItemName }}</div>
                    <div class="text-muted small" *ngIf="item.notes"><em>Note: {{ item.notes }}</em></div>
                  </div>
                </div>
                <span class="fw-bold">₹{{ item.totalPrice.toFixed(2) }}</span>
              </div>

              <div class="d-flex justify-content-between pt-3 border-top mt-2">
                <span class="fw-bold fs-5">Total Amount:</span>
                <span class="fw-bold fs-4 text-dark font-serif">₹{{ order.netAmount.toFixed(2) }}</span>
              </div>
            </div>

            <div class="d-flex justify-content-between align-items-center">
              <a [routerLink]="['/customer/menu']" [queryParams]="{ table: order.tableNumber }" class="btn btn-outline-dark rounded-pill">
                <i class="fa-solid fa-plus me-1"></i> Add More Items
              </a>
              <button class="btn btn-cafe rounded-pill px-4" (click)="requestBill()">
                <i class="fa-solid fa-receipt me-1"></i> Request Bill
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .step-circle { width: 40px; height: 40px; }
  `]
})
export class OrderTrackingComponent implements OnInit, OnDestroy {
  orderId = 0;
  order: Order | null = null;
  private pollSub!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private customerService: CustomerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.orderId = parseInt(this.route.snapshot.paramMap.get('id') || '0');
    this.loadOrderStatus();

    // Poll every 5 seconds for status updates
    this.pollSub = interval(5000).subscribe(() => this.loadOrderStatus());
  }

  ngOnDestroy(): void {
    if (this.pollSub) this.pollSub.unsubscribe();
  }

  loadOrderStatus(): void {
    if (!this.orderId) return;
    this.customerService.getOrderStatus(this.orderId).subscribe(res => this.order = res);
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'NEW': return 'bg-info text-dark';
      case 'PREPARING': return 'bg-warning text-dark';
      case 'READY': return 'bg-success text-white pulse-alert';
      case 'SERVED': return 'bg-secondary text-white';
      case 'PAID': return 'bg-success text-white';
      default: return 'bg-dark text-white';
    }
  }

  getProgressPercent(status: string): number {
    switch (status) {
      case 'NEW': return 25;
      case 'ACCEPTED': return 35;
      case 'PREPARING': return 50;
      case 'READY': return 75;
      case 'SERVED': return 100;
      case 'PAID': return 100;
      default: return 10;
    }
  }

  isStepPassed(step: string): boolean {
    if (!this.order) return false;
    const stages = ['NEW', 'ACCEPTED', 'PREPARING', 'READY', 'SERVED', 'COMPLETED', 'PAID'];
    const currentIdx = stages.indexOf(this.order.status);
    const stepIdx = stages.indexOf(step);
    return currentIdx >= stepIdx;
  }

  requestBill(): void {
    if (!this.order) return;
    this.customerService.requestBill(this.order.tableId).subscribe({
      next: () => this.toastService.show('Bill request sent to cashier!', 'info')
    });
  }
}
