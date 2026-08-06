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

      <!-- Live Requests & Alerts Section (Customer, Chef, Cashier) -->
      <div class="mb-4">
        <!-- Live Alerts Header & Card -->
        <div *ngIf="notifications.length" class="card border-0 bg-warning text-dark shadow-sm rounded-4 p-3 border-start border-5 border-danger">
          <div class="d-flex align-items-center justify-content-between mb-2">
            <div class="d-flex align-items-center gap-3">
              <i class="fa-solid fa-hand-holding-dollar fs-2 pulse-alert text-dark"></i>
              <div>
                <h5 class="fw-bold mb-0">Live Requests (Customer, Chef, Cashier)</h5>
                <div class="small">Accept requests and perform cash collections or table services</div>
              </div>
            </div>
            <button class="btn btn-danger btn-sm rounded-pill font-monospace shadow-sm" (click)="clearAllNotifs()">
              <i class="fa-solid fa-trash-can me-1"></i> Clear All Alerts
            </button>
          </div>

          <div class="row g-3 mt-1">
            <div class="col-md-6" *ngFor="let notif of notifications">
              <div class="bg-white text-dark rounded-3 p-3 shadow-sm border-start border-4"
                   [ngClass]="acceptedCashOrders.has(notif.orderId) ? 'border-success bg-success-subtle' : 'border-warning'">
                <div class="d-flex justify-content-between align-items-start mb-1">
                  <div class="fw-bold text-dark"><i class="fa-solid fa-bell text-warning me-1"></i> {{ notif.title }}</div>
                  <span *ngIf="!acceptedCashOrders.has(notif.orderId)" class="badge bg-warning text-dark font-monospace extra-small">NEW REQUEST</span>
                  <span *ngIf="acceptedCashOrders.has(notif.orderId)" class="badge bg-success text-white font-monospace extra-small">ACCEPTED & IN PROGRESS</span>
                </div>
                <div class="small text-secondary mb-3">{{ notif.message }}</div>

                <div class="d-flex gap-2 flex-wrap">
                  <!-- Cash Request Specific 2-Step Flow -->
                  <ng-container *ngIf="notif.message.includes('CASH') || notif.title.includes('Cash')">
                    <!-- Step 1: Accept Request to Collect Cash -->
                    <button *ngIf="!acceptedCashOrders.has(notif.orderId)"
                            class="btn btn-warning text-dark btn-sm rounded-pill fw-bold shadow-sm"
                            (click)="acceptCashRequest(notif.orderId)">
                      <i class="fa-solid fa-hand-holding-hand me-1"></i> 1. Accept Request & Go to Table
                    </button>

                    <!-- Step 2: Collect Cash & Hand to Cashier -->
                    <button class="btn btn-success btn-sm rounded-pill fw-bold shadow-sm"
                            (click)="confirmCash(notif.orderId, notif.id)">
                      <i class="fa-solid fa-cash-register me-1"></i> 2. Cash Collected -> Hand to Cashier & Provide Receipt
                    </button>
                  </ng-container>

                  <!-- PDF Preview -->
                  <button *ngIf="notif.orderId" class="btn btn-outline-dark btn-sm rounded-pill fw-bold" (click)="downloadPdf(notif.orderId)">
                    <i class="fa-solid fa-file-pdf me-1"></i> Receipt PDF
                  </button>

                  <!-- Dismiss -->
                  <button class="btn btn-light btn-sm text-muted rounded-pill border" (click)="dismissNotif(notif.id)">
                    <i class="fa-solid fa-xmark me-1"></i> Dismiss
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Clean Empty State when no pending requests -->
        <div *ngIf="!notifications.length" class="card border-0 bg-white shadow-sm rounded-4 p-4 border-start border-5 border-success">
          <div class="d-flex align-items-center justify-content-between">
            <div class="d-flex align-items-center gap-3">
              <div class="bg-success-subtle text-success rounded-circle p-3 d-flex align-items-center justify-content-center" style="width: 48px; height: 48px;">
                <i class="fa-solid fa-circle-check fs-3"></i>
              </div>
              <div>
                <h6 class="fw-bold mb-0 text-dark">No Pending Floor Requests</h6>
                <span class="small text-muted">All tables clear. Standing by for real-time requests from Customer, Chef, or Cashier...</span>
              </div>
            </div>
            <span class="badge bg-success text-white rounded-pill px-3 py-2 font-monospace">ONLINE & READY</span>
          </div>
        </div>
      </div>

      <!-- Ready Food Alert Section (Chef -> Waiter -> Customer) -->
      <div *ngIf="readyOrders.length" class="card border-0 bg-success text-white shadow-sm rounded-4 mb-4 p-3 border-start border-5 border-light">
        <div class="d-flex align-items-center justify-content-between">
          <div class="d-flex align-items-center gap-3">
            <i class="fa-solid fa-utensils fs-2 pulse-alert"></i>
            <div>
              <h5 class="fw-bold mb-0">👨‍🍳 {{ readyOrders.length }} Food Order(s) READY from Chef!</h5>
              <div class="small">Accept task, pick up food from Kitchen, and serve to Customer table</div>
            </div>
          </div>
        </div>

        <div class="row g-3 mt-2">
          <div class="col-md-4" *ngFor="let ro of readyOrders">
            <div class="bg-white text-dark rounded-3 p-3 shadow-sm border-start border-4"
                 [ngClass]="acceptedServingOrders.has(ro.id) ? 'border-primary bg-primary-subtle' : 'border-warning'">
              <div class="d-flex justify-content-between fw-bold mb-1">
                <span class="fs-6 text-dark font-monospace">DELIVER TO TABLE #{{ ro.tableNumber }}</span>
                <span class="text-muted">#{{ ro.orderNumber }}</span>
              </div>
              <div class="small text-dark font-monospace mb-1" *ngIf="ro.customerTokenSerial">
                <i class="fa-solid fa-ticket text-warning me-1"></i> Token: <strong>{{ ro.customerTokenSerial }}</strong>
              </div>
              <div class="small text-muted mb-3">Items: {{ ro.items.length }} dishes</div>

              <!-- 2-Step Food Delivery Acceptance Flow -->
              <div class="d-flex flex-column gap-2">
                <!-- Step 1: Accept Task from Chef -->
                <button *ngIf="!acceptedServingOrders.has(ro.id)"
                        class="btn btn-warning text-dark btn-sm rounded-pill fw-bold shadow-sm"
                        (click)="acceptServingTask(ro.id)">
                  <i class="fa-solid fa-hand me-1"></i> 1. Accept Task (Going to Kitchen)
                </button>

                <!-- Step 2: Serve Food to Customer -->
                <button *ngIf="acceptedServingOrders.has(ro.id)"
                        class="btn btn-primary btn-sm rounded-pill fw-bold shadow-sm"
                        (click)="markServed(ro.id)">
                  <i class="fa-solid fa-bell-concierge me-1"></i> 2. Food Picked Up -> Serve to Table #{{ ro.tableNumber }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Restaurant Tables Matrix Grid -->
      <h4 class="font-serif fw-bold mb-3">Table Status Overview</h4>
      <div class="row g-4 mb-5">
        <div class="col-6 col-md-4 col-lg-3" *ngFor="let table of tables; trackBy: trackByTableId">
          <div class="glass-card p-4 text-center h-100 border-start border-4 shadow-sm"
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

            <!-- Direct Status Action Pills -->
            <div class="mt-3">
              <div class="small fw-bold text-muted mb-2 font-monospace">SET STATUS:</div>
              <div class="d-flex flex-column gap-2">
                <button class="btn btn-sm rounded-pill font-monospace fw-bold transition-all"
                        [ngClass]="table.status === 'AVAILABLE' ? 'btn-success shadow-sm' : 'btn-outline-success'"
                        (click)="updateTableStatus(table.id, 'AVAILABLE')">
                  <i class="fa-solid fa-circle-check me-1"></i> AVAILABLE
                </button>
                <button class="btn btn-sm rounded-pill font-monospace fw-bold transition-all"
                        [ngClass]="table.status === 'OCCUPIED' ? 'btn-danger shadow-sm' : 'btn-outline-danger'"
                        (click)="updateTableStatus(table.id, 'OCCUPIED')">
                  <i class="fa-solid fa-lock me-1"></i> OCCUPIED
                </button>
                <button class="btn btn-sm rounded-pill font-monospace fw-bold transition-all"
                        [ngClass]="table.status === 'BILL_REQUESTED' ? 'btn-warning text-dark shadow-sm' : 'btn-outline-warning text-dark'"
                        (click)="updateTableStatus(table.id, 'BILL_REQUESTED')">
                  <i class="fa-solid fa-file-invoice me-1"></i> BILL REQUESTED
                </button>
              </div>
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
  notifications: any[] = [];
  showWalkInModal = false;

  acceptedServingOrders: Set<number> = new Set<number>();
  acceptedCashOrders: Set<number> = new Set<number>();

  private pollSub!: Subscription;
  private wsSub!: Subscription;

  constructor(
    private waiterService: WaiterService,
    private wsService: WebSocketService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadData();
    this.pollSub = interval(4000).subscribe(() => this.loadData());

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
    this.waiterService.getNotifications().subscribe(res => this.notifications = res);
  }

  updateTableStatus(tableId: number, status: string): void {
    this.waiterService.updateTableStatus(tableId, status).subscribe({
      next: () => {
        this.toastService.show(`Table status updated to ${status}`, 'success');
        this.loadData();
      }
    });
  }

  acceptServingTask(orderId: number): void {
    this.acceptedServingOrders.add(orderId);
    this.toastService.show('Task accepted! Pick up food from Kitchen.', 'info');
  }

  acceptCashRequest(orderId: number): void {
    this.acceptedCashOrders.add(orderId);
    this.toastService.show('Cash request accepted! Go to customer table to collect cash.', 'info');
  }

  confirmCash(orderId: number, notifId?: number): void {
    if (!orderId) return;
    this.waiterService.confirmCashPayment(orderId).subscribe({
      next: () => {
        this.toastService.show('Cash collected & handed to Cashier! Downloading thermal receipt...', 'success');
        this.acceptedCashOrders.delete(orderId);
        if (notifId) {
          this.waiterService.dismissNotification(notifId).subscribe();
        }
        this.downloadPdf(orderId);
        this.loadData();
      },
      error: () => this.toastService.show('Error confirming cash payment', 'error')
    });
  }

  dismissNotif(id: number): void {
    this.waiterService.dismissNotification(id).subscribe({
      next: () => {
        this.toastService.show('Request accepted & dismissed', 'info');
        this.loadData();
      }
    });
  }

  clearAllNotifs(): void {
    this.waiterService.clearAllNotifications().subscribe({
      next: () => {
        this.toastService.show('All pending alerts permanently cleared!', 'info');
        this.acceptedCashOrders.clear();
        this.acceptedServingOrders.clear();
        this.loadData();
      }
    });
  }

  downloadPdf(orderId: number): void {
    if (!orderId) return;
    this.waiterService.downloadInvoicePdf(orderId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Invoice_Order_${orderId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.toastService.show('Error downloading invoice PDF', 'error')
    });
  }

  markServed(orderId: number): void {
    this.waiterService.markServed(orderId).subscribe({
      next: () => {
        this.toastService.show('Food served to Customer table!', 'success');
        this.acceptedServingOrders.delete(orderId);
        this.loadData();
      }
    });
  }

  trackByTableId(index: number, table: RestaurantTable): number {
    return table.id;
  }
}
