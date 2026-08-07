import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';
import { RestaurantTable } from '../../core/models/cafe.models';
import { ToastService } from '../../core/services/toast.service';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-table-selection',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="container py-5">
      <!-- Hero Banner Header -->
      <div class="text-center mb-5">
        <span class="badge bg-warning-subtle text-dark border border-warning rounded-pill px-3 py-2 fw-bold mb-3 fs-6">
          <i class="fa-solid fa-qrcode me-2"></i> SMART TABLE QR DIGITAL MENU
        </span>
        <h1 class="font-serif fw-bold display-5 mb-2 text-dark">Welcome to Artisanal Cafe & Bistro</h1>
        <p class="lead text-secondary max-width-600 mx-auto">
          Scan the QR code placed on your table using Google Lens or your smartphone camera to access our live digital menu, customize your order, and send directly to our kitchen.
        </p>
      </div>

      <!-- Tables Grid Section Header -->
      <div class="d-flex align-items-center justify-content-between mb-4 border-bottom pb-3">
        <div>
          <h3 class="font-serif fw-bold mb-0 text-dark"><i class="fa-solid fa-chair me-2 text-warning"></i> Restaurant Tables & QR Codes</h3>
          <span class="text-muted small">Real-Time Table Availability & Digital QR Tokens</span>
        </div>
        <button class="btn btn-outline-dark rounded-pill btn-sm px-3" (click)="loadTables()">
          <i class="fa-solid fa-rotate-right me-1"></i> Refresh Status
        </button>
      </div>

      <!-- Tables Grid -->
      <div class="row g-4">
        <div class="col-md-6 col-lg-4" *ngFor="let tbl of tables">
          <div class="glass-card h-100 p-4 d-flex flex-column justify-content-between position-relative transition-all"
               [ngClass]="{
                 'border-success shadow-sm': tbl.status === 'AVAILABLE',
                 'border-danger opacity-90 bg-light-subtle': tbl.status === 'OCCUPIED',
                 'border-warning': tbl.status === 'BILL_REQUESTED'
               }">
            
            <!-- Table Header -->
            <div>
              <div class="d-flex align-items-center justify-content-between mb-3">
                <span class="badge bg-dark font-monospace fs-6 px-3 py-2 rounded-pill">
                  TABLE #{{ tbl.tableNumber }}
                </span>
                
                <!-- Status Badge -->
                <span class="badge rounded-pill px-3 py-2 fw-bold text-uppercase fs-6 shadow-sm"
                      [ngClass]="{
                        'bg-success text-white': tbl.status === 'AVAILABLE',
                        'bg-danger text-white': tbl.status === 'OCCUPIED',
                        'bg-warning text-dark': tbl.status === 'BILL_REQUESTED'
                      }">
                  <i class="fa-solid me-1" 
                     [ngClass]="{
                       'fa-circle-check': tbl.status === 'AVAILABLE',
                       'fa-lock': tbl.status === 'OCCUPIED',
                       'fa-file-invoice': tbl.status === 'BILL_REQUESTED'
                     }"></i>
                  {{ tbl.status === 'AVAILABLE' ? 'AVAILABLE 🌱' : (tbl.status === 'OCCUPIED' ? 'OCCUPIED 🔴' : 'BILL REQUESTED ⚠️') }}
                </span>
              </div>

              <div *ngIf="tbl.status === 'OCCUPIED' && tbl.currentTokenSerial" class="mb-2">
                <span class="badge bg-danger-subtle text-danger border border-danger font-monospace px-3 py-1 rounded-pill small">
                  <i class="fa-solid fa-ticket me-1"></i> LINKED: {{ tbl.currentTokenSerial }}
                </span>
              </div>

              <!-- Capacity & Info -->
              <div class="d-flex align-items-center gap-2 mb-3 text-secondary">
                <i class="fa-solid fa-users text-warning fs-5"></i>
                <span class="fw-semibold">Capacity: {{ tbl.capacity }} Persons</span>
              </div>

              <!-- QR Code Display Box (Scannable via Google Lens / Camera) -->
              <div class="p-3 bg-white rounded-4 border text-center my-3 shadow-inner position-relative">
                <div class="small fw-bold text-muted mb-2 font-monospace">SCAN VIA GOOGLE LENS</div>
                <img [src]="getQrImageUrl(tbl)" 
                     class="img-fluid rounded-3 mb-2 p-2 bg-white border shadow-sm" 
                     style="max-height: 180px;" 
                     [alt]="'Table ' + tbl.tableNumber + ' QR Code'">
                <div class="extra-small text-muted font-monospace text-truncate">{{ getMenuUrl(tbl) }}</div>
              </div>
            </div>

            <!-- Action Button -->
            <div class="mt-3">
              <button *ngIf="tbl.status === 'AVAILABLE'" 
                      class="btn btn-cafe w-100 py-3 rounded-pill fw-bold shadow-sm" 
                      (click)="selectAvailableTable(tbl)">
                <i class="fa-solid fa-qrcode me-2"></i> Open Table #{{ tbl.tableNumber }} Menu
              </button>

              <button *ngIf="tbl.status !== 'AVAILABLE'" 
                      class="btn btn-outline-danger w-100 py-3 rounded-pill fw-bold" 
                      (click)="showOccupiedWarning(tbl)">
                <i class="fa-solid fa-ban me-2"></i> Table #{{ tbl.tableNumber }} Occupied (Locked)
              </button>
            </div>

          </div>
        </div>
      </div>
    </div>

    <!-- Occupied Warning Modal Overlay -->
    <div class="modal-backdrop fade show" *ngIf="warningTable" (click)="closeWarningModal()" style="z-index: 1050; background-color: rgba(0,0,0,0.7); backdrop-filter: blur(4px);"></div>
    <div class="modal d-block fade show" *ngIf="warningTable" tabindex="-1" style="z-index: 1055;">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden">
          <div class="modal-header bg-danger text-white p-4">
            <h5 class="modal-title font-serif fw-bold d-flex align-items-center gap-2">
              <i class="fa-solid fa-triangle-exclamation fs-3"></i> Table #{{ warningTable.tableNumber }} is Currently Occupied!
            </h5>
            <button type="button" class="btn-close btn-close-white" (click)="closeWarningModal()"></button>
          </div>
          <div class="modal-body p-4 text-center">
            <div class="my-3">
              <i class="fa-solid fa-lock display-2 text-danger mb-3"></i>
              <h4 class="fw-bold text-dark mb-2">This table is taken by another guest</h4>
              <p class="text-secondary">
                Table #{{ warningTable.tableNumber }} currently has an active dining session or bill pending. You cannot open the digital menu or place orders for an occupied table.
              </p>
            </div>
            <div class="p-3 bg-light rounded-3 text-start small">
              <strong class="d-block text-dark mb-1"><i class="fa-solid fa-lightbulb text-warning me-1"></i> What should I do?</strong>
              <span>Please scan or pick any table marked <strong class="text-success">AVAILABLE 🌱</strong> from the table list.</span>
            </div>
          </div>
          <div class="modal-footer bg-light p-3">
            <button type="button" class="btn btn-dark w-100 rounded-pill py-2 fw-bold" (click)="closeWarningModal()">
              Got It, Show Available Tables
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .transition-all { transition: all 0.3s ease; }
  `]
})
export class TableSelectionComponent implements OnInit, OnDestroy {
  tables: RestaurantTable[] = [];
  warningTable: RestaurantTable | null = null;
  private pollSub?: Subscription;

  constructor(
    private customerService: CustomerService,
    private router: Router,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadTables();
    this.pollSub = interval(3000).subscribe(() => this.loadTables());
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  loadTables(): void {
    this.customerService.getTables().subscribe({
      next: (res) => this.tables = res,
      error: () => this.toastService.show('Failed to fetch table list', 'error')
    });
  }

  getMenuUrl(table: RestaurantTable): string {
    const origin = (typeof window !== 'undefined' && window.location && window.location.origin) 
      ? window.location.origin 
      : 'https://cafe-management-zjf6.onrender.com';
    return `${origin}/customer/menu?table=${table.tableNumber}`;
  }

  getQrImageUrl(table: RestaurantTable): string {
    const targetUrl = encodeURIComponent(this.getMenuUrl(table));
    return `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${targetUrl}&color=1a-1a-1a`;
  }

  selectAvailableTable(table: RestaurantTable): void {
    if (table.status !== 'AVAILABLE') {
      this.showOccupiedWarning(table);
      return;
    }
    this.toastService.show(`Opening Digital Menu for Table #${table.tableNumber}...`, 'success');
    this.router.navigate(['/customer/menu'], { queryParams: { table: table.tableNumber } });
  }

  showOccupiedWarning(table: RestaurantTable): void {
    this.warningTable = table;
  }

  closeWarningModal(): void {
    this.warningTable = null;
  }
}
