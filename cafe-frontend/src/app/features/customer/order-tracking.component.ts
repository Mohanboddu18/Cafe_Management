import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';
import { CashierService } from '../../core/services/cashier.service';
import { ToastService } from '../../core/services/toast.service';
import { Order, Invoice } from '../../core/models/cafe.models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-order-tracking',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="container py-5 text-center" *ngIf="!order">
      <div class="glass-card p-5 mx-auto rounded-4 shadow-sm" style="max-width: 600px;">
        <div class="spinner-border text-warning mb-3" style="width: 3rem; height: 3rem;" role="status"></div>
        <h3 class="font-serif fw-bold">Loading Order Details...</h3>
        <p class="text-muted">Fetching live order status and digital bill invoice for your table.</p>
        <a routerLink="/customer/tables" class="btn btn-warning text-dark px-4 py-2 rounded-pill fw-bold">
          <i class="fa-solid fa-table me-2"></i> View All Tables
        </a>
      </div>
    </div>

    <div class="container py-5" *ngIf="order">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="glass-card p-4 p-md-5 mb-4 shadow-lg rounded-4">
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

            <div class="d-flex justify-content-between align-items-center mb-4">
              <a [routerLink]="['/customer/menu']" [queryParams]="{ table: order.tableNumber }" class="btn btn-outline-dark rounded-pill">
                <i class="fa-solid fa-plus me-1"></i> Add More Items
              </a>
              <button class="btn btn-warning text-dark fw-bold rounded-pill px-4" (click)="requestBill()">
                <i class="fa-solid fa-file-invoice-dollar me-1"></i> Request & Pay Bill
              </button>
            </div>

            <!-- DIGITAL THERMAL BILL INVOICE & PAYMENT SECTION -->
            <div *ngIf="invoice" class="mt-4 pt-4 border-top">
              <div class="card border-0 rounded-4 p-4 shadow-sm bg-white border-start border-5 border-success">
                <div class="d-flex align-items-center justify-content-between mb-3">
                  <div>
                    <span class="badge bg-success font-monospace px-3 py-1 text-uppercase">DIGITAL RECEIPT BILL</span>
                    <h4 class="font-serif fw-bold mt-1 mb-0">Invoice #{{ invoice.invoiceNumber }}</h4>
                  </div>
                  <div class="text-end">
                    <span class="badge rounded-pill px-3 py-2 fs-6 fw-bold"
                          [ngClass]="{
                            'bg-success text-white': invoice.paymentStatus === 'COMPLETED',
                            'bg-warning text-dark': invoice.paymentStatus === 'CASH_PENDING_WAITER',
                            'bg-primary text-white': invoice.paymentStatus === 'PENDING'
                          }">
                      {{ invoice.paymentStatus === 'COMPLETED' ? 'PAID ✅' : (invoice.paymentStatus === 'CASH_PENDING_WAITER' ? 'WAITING FOR WAITER 💵' : 'PAYMENT PENDING ⏳') }}
                    </span>
                  </div>
                </div>

                <!-- REALISTIC THERMAL PAPER RECEIPT VISUAL CARD -->
                <div class="thermal-receipt bg-light p-4 rounded-3 mb-4 shadow-sm font-monospace border">
                  <div class="text-center mb-3">
                    <h5 class="fw-bold mb-0 text-dark">ARTISANAL CAFE & BISTRO</h5>
                    <div class="extra-small text-muted">Near Godavari River, Narsapur | Tel: +91 9876543210</div>
                    <div class="dashed-line"></div>
                    <h6 class="fw-bold mb-0 tracking-wider">*** RECEIPT ***</h6>
                  </div>

                  <!-- Receipt Meta Info -->
                  <div class="d-flex justify-content-between extra-small mb-1">
                    <span>INV #: {{ invoice.invoiceNumber }}</span>
                    <span>DATE: {{ invoice.createdAt }}</span>
                  </div>
                  <div class="d-flex justify-content-between extra-small mb-2">
                    <span>TABLE: #{{ order?.tableNumber }}</span>
                    <span>ORDER #: {{ order?.orderNumber }}</span>
                  </div>

                  <div class="dashed-line"></div>

                  <!-- Itemized Table Header -->
                  <div class="d-flex justify-content-between fw-bold extra-small text-uppercase mb-2">
                    <span style="flex: 2;">ITEM</span>
                    <span style="flex: 1;" class="text-center">QTY</span>
                    <span style="flex: 1;" class="text-end">PRICE</span>
                    <span style="flex: 1;" class="text-end">TOTAL</span>
                  </div>

                  <!-- Itemized Lines -->
                  <div *ngFor="let item of invoice.items" class="d-flex justify-content-between extra-small mb-1 text-dark">
                    <span style="flex: 2;" class="text-truncate me-1">{{ item.menuItemName }}</span>
                    <span style="flex: 1;" class="text-center">{{ item.quantity }}</span>
                    <span style="flex: 1;" class="text-end">₹{{ item.unitPrice }}</span>
                    <span style="flex: 1;" class="text-end">₹{{ item.totalPrice }}</span>
                  </div>

                  <div class="dashed-line"></div>

                  <!-- Calculations Summary -->
                  <div class="extra-small">
                    <div class="d-flex justify-content-between mb-1">
                      <span>SUBTOTAL:</span>
                      <span>₹{{ invoice.subtotal.toFixed(2) }}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-1 text-success" *ngIf="invoice.discount > 0">
                      <span>DISCOUNT ({{ invoice.couponCode || 'PROMO' }}):</span>
                      <span>-₹{{ invoice.discount.toFixed(2) }}</span>
                    </div>
                    <div class="d-flex justify-content-between mb-1">
                      <span>GST TAX (5%):</span>
                      <span>+₹{{ invoice.gstAmount.toFixed(2) }}</span>
                    </div>
                    <div class="dashed-line"></div>
                    <div class="d-flex justify-content-between fw-bold fs-6 text-dark my-1">
                      <span>TOTAL PAYABLE:</span>
                      <span class="text-success">₹{{ invoice.totalPayable.toFixed(2) }}</span>
                    </div>
                    <div class="d-flex justify-content-between extra-small text-secondary">
                      <span>PAYMENT METHOD:</span>
                      <span class="fw-bold text-dark">{{ invoice.paymentMethod || 'PENDING' }}</span>
                    </div>
                  </div>

                  <div class="dashed-line"></div>

                  <!-- Receipt Footer -->
                  <div class="text-center extra-small text-muted mt-2">
                    <div>THANK YOU FOR DINING WITH US!</div>
                    <div>PLEASE VISIT AGAIN</div>
                    <div class="barcode-line mt-2 text-dark font-monospace fs-5">||| || ||||| |||| || |||| |||</div>
                  </div>
                </div>

                <!-- PAYMENT STATUS: CASH PENDING WAITER ALERT -->
                <div *ngIf="invoice.paymentStatus === 'CASH_PENDING_WAITER'" class="alert alert-warning rounded-3 p-3 mb-3 text-center shadow-sm">
                  <i class="fa-solid fa-money-bill-wave fs-2 text-warning mb-2 d-block"></i>
                  <h5 class="fw-bold mb-1">Cash Payment Requested</h5>
                  <p class="mb-0 small text-dark">
                    Please hand <strong class="fs-5 text-success">₹{{ invoice.totalPayable.toFixed(2) }}</strong> in <strong>CASH</strong> to your Waiter at Table #{{ order.tableNumber }}.
                    Once your Waiter collects and confirms the cash, your downloadable PDF receipt will be unlocked automatically!
                  </p>
                </div>

                <!-- PAYMENT STATUS: COMPLETED RECEIPT & DOWNLOAD -->
                <div *ngIf="invoice.paymentStatus === 'COMPLETED'" class="alert alert-success rounded-3 p-4 mb-4 text-center shadow-sm">
                  <i class="fa-solid fa-circle-check fs-1 text-success mb-2 d-block"></i>
                  <h4 class="fw-bold mb-1">Payment Completed!</h4>
                  <p class="small text-secondary mb-3">Thank you for dining with us at Artisanal Cafe & Bistro!</p>
                  <button class="btn btn-success btn-lg rounded-pill fw-bold px-4 shadow" (click)="downloadPdf()">
                    <i class="fa-solid fa-file-pdf me-2"></i> Download Thermal Receipt PDF
                  </button>
                </div>

                <!-- OPTIONAL POST-PAYMENT DISH RATING & FEEDBACK CARD -->
                <div *ngIf="invoice.paymentStatus === 'COMPLETED'" class="card border-0 glass-card p-4 mb-4 shadow-sm">
                  <div class="d-flex align-items-center gap-3 mb-3">
                    <div class="bg-warning-subtle text-warning p-3 rounded-circle border border-warning">
                      <i class="fa-solid fa-star fs-3"></i>
                    </div>
                    <div>
                      <h4 class="font-serif fw-bold mb-1 text-dark">Rate Your Dishes & Experience (Optional)</h4>
                      <span class="text-secondary small">Your ratings help us maintain gourmet standards and update product scores on our live menu!</span>
                    </div>
                  </div>

                  <div *ngIf="!reviewSubmitted">
                    <div class="row g-3 mb-4">
                      <div class="col-12" *ngFor="let item of order?.items">
                        <div class="p-3 bg-light rounded-4 border d-flex flex-column flex-md-row align-items-md-center justify-content-between gap-3">
                          <div class="d-flex align-items-center gap-3">
                            <img [src]="item.imageUrl || 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80'" 
                                 class="rounded-3 object-fit-cover shadow-sm" style="width: 55px; height: 55px;" [alt]="item.menuItemName">
                            <div>
                              <h6 class="fw-bold mb-0 text-dark">{{ item.menuItemName }}</h6>
                              <span class="text-muted extra-small">Quantity: {{ item.quantity }} x ₹{{ item.unitPrice.toFixed(2) }}</span>
                            </div>
                          </div>

                          <!-- 5 Star Interactive Picker -->
                          <div class="d-flex align-items-center gap-2">
                            <span class="small fw-bold text-secondary me-2">Rating:</span>
                            <div class="d-flex gap-1 fs-5">
                              <i *ngFor="let star of [1, 2, 3, 4, 5]" 
                                 class="fa-star cursor-pointer transition-all"
                                 [ngClass]="star <= getRating(item.menuItemId) ? 'fa-solid text-warning' : 'fa-regular text-muted'"
                                 (click)="setRating(item.menuItemId, star)"></i>
                            </div>
                            <span class="badge bg-warning text-dark font-monospace ms-2">{{ getRating(item.menuItemId) }}.0 / 5.0</span>
                          </div>
                        </div>

                        <!-- Optional Comment per Item -->
                        <div class="mt-2 px-1">
                          <input type="text" class="form-control form-control-sm rounded-pill font-monospace" 
                                 placeholder="Optional feedback for {{ item.menuItemName }} (e.g. Delicious taste & aroma)" 
                                 [(ngModel)]="itemCommentsMap[item.menuItemId]">
                        </div>
                      </div>
                    </div>

                    <button class="btn btn-warning text-dark btn-lg w-100 rounded-pill fw-bold shadow-sm" 
                            [disabled]="submittingReview" 
                            (click)="submitItemReviews()">
                      <i class="fa-solid fa-paper-plane me-2"></i> Submit Ratings & Feedback
                    </button>
                  </div>

                  <div *ngIf="reviewSubmitted" class="alert alert-success border-0 rounded-4 text-center p-3 mb-0">
                    <i class="fa-solid fa-heart text-danger fs-3 mb-2 d-block"></i>
                    <h5 class="fw-bold text-dark mb-1">Thank You For Your Review! ⭐⭐⭐⭐⭐</h5>
                    <span class="text-secondary small">Your dish ratings have been recorded and updated on our digital menu!</span>
                  </div>
                </div>

                <!-- PAYMENT SELECTION OPTIONS (If PENDING or UNPAID) -->
                <div *ngIf="invoice.paymentStatus === 'PENDING' || invoice.paymentStatus === 'UNPAID'">
                  <h5 class="font-serif fw-bold mb-3">Select Payment Method</h5>

                  <div class="row g-3 mb-4">
                    <!-- Option 1: UPI -->
                    <div class="col-md-4">
                      <div class="card p-3 h-100 text-center cursor-pointer transition-all border-2"
                           [ngClass]="selectedPaymentMethod === 'UPI' ? 'border-success bg-success-subtle shadow-sm' : 'border-light bg-light'"
                           (click)="selectedPaymentMethod = 'UPI'">
                        <i class="fa-solid fa-qrcode fs-2 text-success mb-2"></i>
                        <h6 class="fw-bold mb-1 text-dark">UPI Payment</h6>
                        <span class="extra-small text-muted">GPay, PhonePe, Paytm</span>
                      </div>
                    </div>

                    <!-- Option 2: CARD -->
                    <div class="col-md-4">
                      <div class="card p-3 h-100 text-center cursor-pointer transition-all border-2"
                           [ngClass]="selectedPaymentMethod === 'CARD' ? 'border-primary bg-primary-subtle shadow-sm' : 'border-light bg-light'"
                           (click)="selectedPaymentMethod = 'CARD'">
                        <i class="fa-solid fa-credit-card fs-2 text-primary mb-2"></i>
                        <h6 class="fw-bold mb-1 text-dark">Card Payment</h6>
                        <span class="extra-small text-muted">Credit / Debit Card</span>
                      </div>
                    </div>

                    <!-- Option 3: CASH -->
                    <div class="col-md-4">
                      <div class="card p-3 h-100 text-center cursor-pointer transition-all border-2"
                           [ngClass]="selectedPaymentMethod === 'CASH' ? 'border-warning bg-warning-subtle shadow-sm' : 'border-light bg-light'"
                           (click)="selectedPaymentMethod = 'CASH'">
                        <i class="fa-solid fa-money-bill-wave fs-2 text-warning mb-2"></i>
                        <h6 class="fw-bold mb-1 text-dark">Pay Cash to Waiter</h6>
                        <span class="extra-small text-muted">Hand physical cash at table</span>
                      </div>
                    </div>
                  </div>

                  <!-- UPI PAYMENT FORM -->
                  <div *ngIf="selectedPaymentMethod === 'UPI'" class="p-3 bg-light rounded-4 mb-3 border">
                    <div class="text-center mb-3">
                      <div class="small fw-bold text-muted mb-2 font-monospace">SCAN UPI QR CODE TO PAY</div>
                      <img [src]="getUpiQrUrl()" class="img-fluid rounded-3 bg-white p-2 border shadow-sm mb-2" style="max-height: 180px;" alt="UPI QR">
                      <div class="extra-small text-muted font-monospace">UPI ID: artisanalcafe&#64;upi</div>
                    </div>
                    <div class="form-floating mb-3">
                      <input type="text" class="form-control rounded-3" id="upiRef" placeholder="UPI Ref / Transaction ID" [(ngModel)]="upiRef">
                      <label for="upiRef">UPI Transaction Ref ID (Optional)</label>
                    </div>
                    <button class="btn btn-success w-100 py-3 rounded-pill fw-bold shadow-sm" (click)="payOnline('UPI')">
                      <i class="fa-solid fa-check me-2"></i> Confirm UPI Payment (₹{{ invoice.totalPayable.toFixed(2) }})
                    </button>
                  </div>

                  <!-- CARD PAYMENT FORM -->
                  <div *ngIf="selectedPaymentMethod === 'CARD'" class="p-3 bg-light rounded-4 mb-3 border">
                    <div class="form-floating mb-3">
                      <input type="text" class="form-control rounded-3" id="cardNum" placeholder="1234 5678 9012 3456" [(ngModel)]="cardNumber">
                      <label for="cardNum">Card Number (Mock)</label>
                    </div>
                    <button class="btn btn-primary w-100 py-3 rounded-pill fw-bold shadow-sm" (click)="payOnline('CARD')">
                      <i class="fa-solid fa-lock me-2"></i> Pay ₹{{ invoice.totalPayable.toFixed(2) }} via Card
                    </button>
                  </div>

                  <!-- CASH PAYMENT FORM -->
                  <div *ngIf="selectedPaymentMethod === 'CASH'" class="p-3 bg-warning-subtle rounded-4 mb-3 border border-warning text-center">
                    <p class="mb-3 text-dark">
                      You are choosing to pay <strong class="fs-5 text-success">₹{{ invoice.totalPayable.toFixed(2) }}</strong> in <strong>CASH</strong> to your Waiter.
                      After clicking confirm, your Waiter will come to Table #{{ order.tableNumber }} to collect the cash and submit it to the Cashier.
                    </p>
                    <button class="btn btn-warning text-dark w-100 py-3 rounded-pill fw-bold shadow-sm" (click)="payCashToWaiter()">
                      <i class="fa-solid fa-hand-holding-dollar me-2"></i> Request Waiter to Collect Cash (₹{{ invoice.totalPayable.toFixed(2) }})
                    </button>
                  </div>

                </div>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .step-circle { width: 40px; height: 40px; }
    .cursor-pointer { cursor: pointer; }
    .transition-all { transition: all 0.3s ease; }
    .extra-small { font-size: 0.78rem; }
    .thermal-receipt {
      background: #ffffff;
      border-radius: 8px;
      position: relative;
      border-top: 6px solid #28a745;
    }
    .dashed-line {
      border-top: 2px dashed #ccc;
      margin: 10px 0;
    }
    .barcode-line {
      letter-spacing: 5px;
      font-weight: bold;
    }
  `]
})
export class OrderTrackingComponent implements OnInit, OnDestroy {
  orderId = 0;
  order: Order | null = null;
  invoice: Invoice | null = null;
  selectedPaymentMethod: 'UPI' | 'CARD' | 'CASH' = 'UPI';
  upiRef = '';
  cardNumber = '';
  private pollSub!: Subscription;

  constructor(
    private route: ActivatedRoute,
    private customerService: CustomerService,
    private cashierService: CashierService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.orderId = parseInt(this.route.snapshot.paramMap.get('id') || '0');
    this.loadOrderStatus();

    // Poll every 4 seconds for status and bill updates
    this.pollSub = interval(4000).subscribe(() => this.loadOrderStatus());
  }

  ngOnDestroy(): void {
    if (this.pollSub) this.pollSub.unsubscribe();
  }

  loadOrderStatus(): void {
    if (!this.orderId) return;
    this.customerService.getOrderStatus(this.orderId).subscribe(res => {
      this.order = res;
      if (res.status === 'BILL_REQUESTED' || res.status === 'PAID') {
        this.fetchInvoice();
      }
    });
  }

  fetchInvoice(): void {
    this.customerService.getInvoice(this.orderId).subscribe({
      next: (inv) => this.invoice = inv,
      error: () => {}
    });
  }

  getUpiQrUrl(): string {
    const amount = this.invoice?.totalPayable || 0;
    const upiData = encodeURIComponent(`upi://pay?pa=artisanalcafe@upi&pn=Artisanal%20Cafe&am=${amount}&tn=Order_${this.order?.orderNumber}`);
    return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${upiData}`;
  }

  payOnline(method: 'UPI' | 'CARD'): void {
    if (!this.orderId) return;
    const ref = method === 'UPI' ? (this.upiRef || 'UPI-' + Math.floor(100000 + Math.random() * 900000)) : 'CARD-MOCK';
    this.customerService.payInvoice(this.orderId, method, ref).subscribe({
      next: (inv) => {
        this.invoice = inv;
        this.toastService.show(`Payment of ₹${inv.totalPayable} completed via ${method}! Table is now free.`, 'success');
        this.loadOrderStatus();
      },
      error: (err) => this.toastService.show(err.error?.message || 'Payment failed', 'error')
    });
  }

  payCashToWaiter(): void {
    if (!this.orderId) return;
    this.customerService.payInvoice(this.orderId, 'CASH').subscribe({
      next: (inv) => {
        this.invoice = inv;
        this.toastService.show(`Cash payment requested. Please hand ₹${inv.totalPayable} cash to your Waiter!`, 'info');
        this.loadOrderStatus();
      },
      error: (err) => this.toastService.show(err.error?.message || 'Error processing cash request', 'error')
    });
  }

  downloadPdf(): void {
    if (!this.orderId) return;
    this.customerService.downloadInvoicePdf(this.orderId).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `Invoice_Order_${this.orderId}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  getStatusBadgeClass(status: string): string {
    switch (status) {
      case 'NEW': return 'bg-info text-dark';
      case 'PREPARING': return 'bg-warning text-dark';
      case 'READY': return 'bg-success text-white pulse-alert';
      case 'SERVED': return 'bg-secondary text-white';
      case 'BILL_REQUESTED': return 'bg-warning text-dark pulse-alert';
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
      case 'SERVED': return 90;
      case 'BILL_REQUESTED': return 95;
      case 'PAID': return 100;
      default: return 10;
    }
  }

  isStepPassed(step: string): boolean {
    if (!this.order) return false;
    const stages = ['NEW', 'ACCEPTED', 'PREPARING', 'READY', 'SERVED', 'BILL_REQUESTED', 'PAID'];
    const currentIdx = stages.indexOf(this.order.status);
    const stepIdx = stages.indexOf(step);
    return currentIdx >= stepIdx;
  }

  requestBill(): void {
    if (!this.order) return;
    this.customerService.requestBill(this.order.tableId).subscribe({
      next: () => {
        this.toastService.show('Bill request sent to Cashier!', 'info');
        this.fetchInvoice();
      }
    });
  }

  itemRatingsMap: { [menuItemId: number]: number } = {};
  itemCommentsMap: { [menuItemId: number]: string } = {};
  reviewSubmitted = false;
  submittingReview = false;

  setRating(menuItemId: number, rating: number): void {
    this.itemRatingsMap[menuItemId] = rating;
  }

  getRating(menuItemId: number): number {
    return this.itemRatingsMap[menuItemId] || 5;
  }

  submitItemReviews(): void {
    if (!this.order) return;
    this.submittingReview = true;

    const ratings = this.order.items.map(item => ({
      menuItemId: item.menuItemId,
      rating: this.getRating(item.menuItemId),
      comment: this.itemCommentsMap[item.menuItemId] || ''
    }));

    const req = {
      orderId: this.order.id,
      customerName: this.order.customerName,
      ratings: ratings
    };

    this.customerService.submitReview(req).subscribe({
      next: () => {
        this.submittingReview = false;
        this.reviewSubmitted = true;
        this.toastService.show('Thank you! Your dish ratings and feedback have been recorded & updated on the menu!', 'success');
      },
      error: () => {
        this.submittingReview = false;
        this.toastService.show('Failed to submit ratings', 'error');
      }
    });
  }
}
