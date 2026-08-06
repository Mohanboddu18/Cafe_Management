import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CashierService } from '../../core/services/cashier.service';
import { CustomerService } from '../../core/services/customer.service';
import { ToastService } from '../../core/services/toast.service';
import { RestaurantTable, Order, Invoice } from '../../core/models/cafe.models';

@Component({
  selector: 'app-cashier-billing',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container py-4">
      <div class="d-flex align-items-center justify-content-between mb-4 bg-dark text-white p-3 rounded-4 shadow-sm">
        <div class="d-flex align-items-center gap-3">
          <div class="bg-warning text-dark rounded-circle p-3 d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
            <i class="fa-solid fa-file-invoice-dollar fs-3"></i>
          </div>
          <div>
            <h3 class="font-serif fw-bold mb-0">Cashier Billing & Checkout</h3>
            <span class="text-white-50 small">Process Table Payments, Apply Discounts & Generate PDF Invoices</span>
          </div>
        </div>
      </div>

      <div class="row g-4">
        <!-- Left: Table Selection -->
        <div class="col-lg-4">
          <div class="glass-card p-4 h-100">
            <h5 class="font-serif fw-bold mb-3"><i class="fa-solid fa-chair me-2"></i> Select Active Table</h5>
            <div class="list-group list-group-flush rounded-4 overflow-hidden border">
              <button *ngFor="let tbl of tables" 
                      class="list-group-item list-group-item-action d-flex justify-content-between align-items-center p-3"
                      [ngClass]="{ 'active': selectedTable?.id === tbl.id }"
                      (click)="selectTable(tbl)">
                <div>
                  <h6 class="fw-bold mb-0">Table #{{ tbl.tableNumber }}</h6>
                  <span class="small" [ngClass]="selectedTable?.id === tbl.id ? 'text-white-50' : 'text-muted'">Cap: {{ tbl.capacity }}</span>
                </div>
                <span class="badge rounded-pill"
                      [ngClass]="{
                        'bg-success': tbl.status === 'AVAILABLE',
                        'bg-danger': tbl.status === 'OCCUPIED',
                        'bg-warning text-dark pulse-alert': tbl.status === 'BILL_REQUESTED'
                      }">
                  {{ tbl.status }}
                </span>
              </button>
            </div>
          </div>
        </div>

        <!-- Right: Invoice & Billing Processing -->
        <div class="col-lg-8">
          <div *ngIf="!selectedOrder" class="glass-card p-5 text-center my-auto text-muted">
            <i class="fa-solid fa-receipt display-1 mb-3 text-light"></i>
            <h5>No Order Loaded</h5>
            <p>Select a table with an active order from the left panel to begin billing.</p>
          </div>

          <div *ngIf="selectedOrder" class="glass-card p-4">
            <div class="d-flex justify-content-between align-items-center border-bottom pb-3 mb-3">
              <div>
                <span class="badge bg-warning text-dark font-monospace mb-1">TABLE #{{ selectedOrder.tableNumber }}</span>
                <h4 class="font-serif fw-bold mb-0">Order #{{ selectedOrder.orderNumber }}</h4>
                <div class="text-muted small">Customer: {{ selectedOrder.customerName }}</div>
              </div>
              <div class="text-end">
                <span class="badge bg-dark text-white fs-6">{{ selectedOrder.status }}</span>
              </div>
            </div>

            <!-- Itemized Table -->
            <table class="table table-hover align-middle mb-4">
              <thead class="table-light">
                <tr>
                  <th>Item</th>
                  <th class="text-center">Qty</th>
                  <th class="text-end">Unit Price</th>
                  <th class="text-end">Total</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of selectedOrder.items">
                  <td class="fw-bold">{{ item.menuItemName }}</td>
                  <td class="text-center">{{ item.quantity }}</td>
                  <td class="text-end">₹{{ item.unitPrice.toFixed(2) }}</td>
                  <td class="text-end fw-bold">₹{{ item.totalPrice.toFixed(2) }}</td>
                </tr>
              </tbody>
            </table>

            <!-- Coupon Application -->
            <div class="row g-3 mb-4 bg-light p-3 rounded-4 align-items-center">
              <div class="col-md-6">
                <div class="input-group">
                  <input type="text" class="form-control text-uppercase" placeholder="Coupon Code (e.g. WELCOME10)" [(ngModel)]="couponCode">
                  <button class="btn btn-dark" (click)="applyCoupon()">Apply</button>
                </div>
              </div>
              <div class="col-md-6 text-end">
                <div *ngIf="appliedDiscount > 0" class="text-success fw-bold">
                  <i class="fa-solid fa-tag me-1"></i> Coupon Discount: -₹{{ appliedDiscount.toFixed(2) }}
                </div>
              </div>
            </div>

            <!-- Calculation Summary -->
            <div class="card border-0 bg-light rounded-4 p-3 mb-4">
              <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">Subtotal:</span>
                <span class="fw-bold">₹{{ selectedOrder.totalAmount.toFixed(2) }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2 text-danger" *ngIf="appliedDiscount > 0">
                <span>Discount:</span>
                <span>-₹{{ appliedDiscount.toFixed(2) }}</span>
              </div>
              <div class="d-flex justify-content-between mb-2">
                <span class="text-muted">GST Tax (5%):</span>
                <span class="fw-bold">+₹{{ gstAmount.toFixed(2) }}</span>
              </div>
              <div class="d-flex justify-content-between pt-2 border-top">
                <span class="fw-bold fs-4 font-serif">TOTAL PAYABLE:</span>
                <span class="fw-bold fs-3 text-dark font-serif">₹{{ totalPayable.toFixed(2) }}</span>
              </div>
            </div>

            <!-- Payment Method & Checkout -->
            <div class="mb-4">
              <label class="form-label fw-bold">Payment Method</label>
              <div class="d-flex gap-3">
                <div class="form-check flex-grow-1 p-3 border rounded-3 text-center" [ngClass]="{ 'bg-warning-subtle border-warning': paymentMethod === 'CASH' }">
                  <input class="form-check-input" type="radio" name="payMethod" id="cash" value="CASH" [(ngModel)]="paymentMethod">
                  <label class="form-check-label fw-bold d-block" for="cash"><i class="fa-solid fa-money-bill-wave me-1"></i> CASH</label>
                </div>
                <div class="form-check flex-grow-1 p-3 border rounded-3 text-center" [ngClass]="{ 'bg-warning-subtle border-warning': paymentMethod === 'CARD' }">
                  <input class="form-check-input" type="radio" name="payMethod" id="card" value="CARD" [(ngModel)]="paymentMethod">
                  <label class="form-check-label fw-bold d-block" for="card"><i class="fa-regular fa-credit-card me-1"></i> CARD</label>
                </div>
                <div class="form-check flex-grow-1 p-3 border rounded-3 text-center" [ngClass]="{ 'bg-warning-subtle border-warning': paymentMethod === 'UPI' }">
                  <input class="form-check-input" type="radio" name="payMethod" id="upi" value="UPI" [(ngModel)]="paymentMethod">
                  <label class="form-check-label fw-bold d-block" for="upi"><i class="fa-solid fa-qrcode me-1"></i> UPI / QR</label>
                </div>
              </div>
            </div>

            <button class="btn btn-cafe w-100 py-3 rounded-pill fw-bold fs-5" (click)="processPayment()" [disabled]="processing">
              <span *ngIf="processing" class="spinner-border spinner-border-sm me-2"></span>
              <i class="fa-solid fa-print me-2"></i> Process Payment & Print Invoice PDF
            </button>
          </div>
        </div>
      </div>
    </div>
  `
})
export class CashierBillingComponent implements OnInit {
  tables: RestaurantTable[] = [];
  selectedTable: RestaurantTable | null = null;
  selectedOrder: Order | null = null;

  couponCode = '';
  appliedDiscount = 0;
  paymentMethod = 'CASH';
  processing = false;

  constructor(
    private cashierService: CashierService,
    private customerService: CustomerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadTables();
  }

  loadTables(): void {
    this.cashierService.getActiveTables().subscribe(res => this.tables = res);
  }

  selectTable(table: RestaurantTable): void {
    this.selectedTable = table;
    this.couponCode = '';
    this.appliedDiscount = 0;
    this.customerService.getActiveOrderByTable(table.id).subscribe({
      next: (order) => this.selectedOrder = order,
      error: () => {
        this.selectedOrder = null;
        this.toastService.show(`No active order for Table #${table.tableNumber}`, 'info');
      }
    });
  }

  applyCoupon(): void {
    if (!this.selectedOrder || !this.couponCode) return;
    this.cashierService.applyCoupon(this.couponCode, this.selectedOrder.totalAmount).subscribe({
      next: (res) => {
        if (res.valid) {
          this.appliedDiscount = res.discountAmount;
          this.toastService.show(res.message, 'success');
        } else {
          this.toastService.show(res.message, 'error');
        }
      }
    });
  }

  get gstAmount(): number {
    if (!this.selectedOrder) return 0;
    const subAfterDisc = this.selectedOrder.totalAmount - this.appliedDiscount;
    return subAfterDisc * 0.05; // 5% GST
  }

  get totalPayable(): number {
    if (!this.selectedOrder) return 0;
    return (this.selectedOrder.totalAmount - this.appliedDiscount) + this.gstAmount;
  }

  processPayment(): void {
    if (!this.selectedOrder) return;
    this.processing = true;

    const payload = {
      orderId: this.selectedOrder.id,
      couponCode: this.couponCode,
      discountAmount: this.appliedDiscount,
      gstPercentage: 5.0,
      paymentMethod: this.paymentMethod,
      transactionRef: 'TXN-' + Date.now()
    };

    this.cashierService.processPayment(payload).subscribe({
      next: (inv) => {
        this.processing = false;
        this.toastService.show(`Payment COMPLETED for Invoice #${inv.invoiceNumber}!`, 'success');
        
        // Trigger PDF Download
        this.cashierService.downloadInvoicePdf(this.selectedOrder!.id).subscribe(blob => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `Invoice_${inv.invoiceNumber}.pdf`;
          a.click();
        });

        this.selectedOrder = null;
        this.selectedTable = null;
        this.loadTables();
      },
      error: (err) => {
        this.processing = false;
        this.toastService.show('Payment processing error', 'error');
      }
    });
  }
}
