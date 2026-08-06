import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { CustomerService } from '../../core/services/customer.service';
import { ToastService } from '../../core/services/toast.service';
import { Category, MenuItem, Cart, Order, RestaurantTable } from '../../core/models/cafe.models';

@Component({
  selector: 'app-customer-menu',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="container py-4">
      <!-- Table QR Header Banner -->
      <div class="glass-card p-4 mb-4 text-white position-relative overflow-hidden" 
           style="background: linear-gradient(135deg, #6F4E37, #382417);">
        <div class="row align-items-center">
          <div class="col-md-8">
            <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
              <span class="badge bg-warning text-dark font-monospace px-3 py-2 fs-6">
                <i class="fa-solid fa-qrcode me-1"></i> SCANNED TABLE #{{ tableNumber }}
              </span>

              <!-- Live Table Occupancy Status Tag -->
              <span class="badge rounded-pill px-3 py-2 fs-6 fw-bold shadow-sm"
                    [ngClass]="{
                      'bg-success text-white': !isTableOccupiedByOther && tableStatus === 'AVAILABLE',
                      'bg-primary text-white': !isTableOccupiedByOther && activeOrder,
                      'bg-danger text-white pulse-alert': isTableOccupiedByOther
                    }">
                <i class="fa-solid me-1" 
                   [ngClass]="{
                     'fa-circle-check': !isTableOccupiedByOther && tableStatus === 'AVAILABLE',
                     'fa-utensils': !isTableOccupiedByOther && activeOrder,
                     'fa-lock': isTableOccupiedByOther
                   }"></i>
                {{ isTableOccupiedByOther ? 'STATUS: OCCUPIED BY ANOTHER GUEST 🔴' : (activeOrder ? 'STATUS: DINING (ACTIVE ORDER) 🟢' : 'STATUS: AVAILABLE 🌱') }}
              </span>
            </div>
            <h1 class="font-serif display-6 fw-bold mb-2">Artisanal Digital Menu</h1>
            <p class="mb-0 text-white-50">Browse our gourmet creations, customize your order, and send directly to our kitchen.</p>
          </div>
          <div class="col-md-4 text-end">
            <a [routerLink]="['/customer/tables']" class="btn btn-outline-light rounded-pill px-3 me-2 mb-1">
              <i class="fa-solid fa-chair me-1"></i> Switch Table
            </a>
            <a *ngIf="activeOrder" [routerLink]="['/customer/tracking', activeOrder.id]" class="btn btn-light rounded-pill px-3 mb-1">
              <i class="fa-solid fa-clock-rotate-left me-1"></i> Track Order #{{ activeOrder.orderNumber }}
            </a>
          </div>
        </div>
      </div>

      <!-- Occupied Warning Banner if Table is Occupied by another customer -->
      <div *ngIf="isTableOccupiedByOther" class="alert alert-danger border-2 border-danger rounded-4 shadow p-4 mb-4">
        <div class="d-flex flex-column flex-md-row align-items-center justify-content-between gap-3">
          <div class="d-flex align-items-center gap-3">
            <i class="fa-solid fa-lock display-5 text-danger"></i>
            <div>
              <h4 class="fw-bold mb-1 text-danger">Table #{{ tableNumber }} is Currently OCCUPIED!</h4>
              <span class="text-secondary">Another customer is currently dining at this table. Ordering is locked. Please pick a free table to enjoy your meal.</span>
            </div>
          </div>
          <a [routerLink]="['/customer/tables']" class="btn btn-danger btn-lg rounded-pill px-4 fw-bold shadow-sm text-nowrap">
            <i class="fa-solid fa-chair me-2"></i> Pick Available Free Table
          </a>
        </div>
      </div>

      <!-- Active Order Status Ribbon if any -->
      <div *ngIf="activeOrder" class="alert alert-warning d-flex align-items-center justify-content-between rounded-4 shadow-sm mb-4">
        <div>
          <i class="fa-solid fa-fire-burner fs-4 me-2"></i>
          <strong>Active Order #{{ activeOrder.orderNumber }}</strong>: Status is 
          <span class="badge bg-dark text-warning ms-1 fs-6">{{ activeOrder.status }}</span>
        </div>
        <div class="d-flex gap-2">
          <a [routerLink]="['/customer/tracking', activeOrder.id]" class="btn btn-dark btn-sm rounded-pill px-3">
            Live Track
          </a>
          <button class="btn btn-outline-dark btn-sm rounded-pill px-3" (click)="onRequestBill()">
            <i class="fa-solid fa-receipt me-1"></i> Request Bill
          </button>
        </div>
      </div>

      <!-- Filters & Search Bar -->
      <div class="row g-3 mb-4">
        <div class="col-md-6">
          <div class="input-group shadow-sm rounded-pill overflow-hidden">
            <span class="input-group-text bg-white border-0 ps-3"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
            <input type="text" class="form-control border-0 py-2" placeholder="Search delicious coffee, pizzas, burgers, shawarmas..." [(ngModel)]="searchQuery" (ngModelChange)="filterMenu()">
          </div>
        </div>
        <div class="col-md-6 d-flex align-items-center justify-content-md-end gap-3 flex-wrap">
          <!-- 3-Way Veg / Non-Veg / All Dietary Filter -->
          <div class="btn-group rounded-pill overflow-hidden p-1 bg-light border shadow-sm" role="group">
            <button type="button" 
                    class="btn btn-sm rounded-pill px-3 fw-bold transition-all"
                    [ngClass]="foodTypeFilter === 'ALL' ? 'btn-cafe shadow-sm' : 'btn-light text-dark'"
                    (click)="setFoodTypeFilter('ALL')">
              ALL
            </button>
            <button type="button" 
                    class="btn btn-sm rounded-pill px-3 fw-bold transition-all text-success"
                    [ngClass]="foodTypeFilter === 'VEG' ? 'btn-success text-white shadow-sm' : 'btn-light'"
                    (click)="setFoodTypeFilter('VEG')">
              🌱 VEG ONLY
            </button>
            <button type="button" 
                    class="btn btn-sm rounded-pill px-3 fw-bold transition-all text-danger"
                    [ngClass]="foodTypeFilter === 'NON_VEG' ? 'btn-danger text-white shadow-sm' : 'btn-light'"
                    (click)="setFoodTypeFilter('NON_VEG')">
              🍗 NON-VEG ONLY
            </button>
          </div>

          <button class="btn btn-outline-dark rounded-pill position-relative" (click)="toggleCartDrawer()">
            <i class="fa-solid fa-basket-shopping me-1"></i> Cart
            <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" *ngIf="cart?.items?.length">
              {{ cartCount }}
            </span>
          </button>
        </div>
      </div>

    <!-- Category Filter Pills -->
      <div class="d-flex gap-2 overflow-auto pb-3 mb-4 flex-nowrap" style="scrollbar-width: thin;">
        <button class="btn rounded-pill px-4 fw-semibold text-nowrap"
                [ngClass]="selectedCategoryId === null ? 'btn-cafe' : 'btn-outline-secondary'"
                (click)="selectCategory(null)">
          All Categories
        </button>
        <button *ngFor="let cat of categories" 
                class="btn rounded-pill px-4 fw-semibold text-nowrap"
                [ngClass]="selectedCategoryId === cat.id ? 'btn-cafe' : 'btn-outline-secondary'"
                (click)="selectCategory(cat.id)">
          {{ cat.name }}
        </button>
      </div>

      <!-- Menu Grid -->
      <div class="row g-4">
        <div class="col-md-6 col-lg-4" *ngFor="let item of filteredMenuItems">
          <div class="glass-card h-100 overflow-hidden d-flex flex-column hover-lift" style="cursor: pointer;" (click)="openItemModal(item)">
            <div class="position-relative" style="height: 200px; overflow: hidden;">
              <img [src]="item.imageUrl || defaultFallbackImg" 
                   (error)="onImgError($event)"
                   class="w-100 h-100 object-fit-cover transition-scale" [alt]="item.name">
              <span class="position-absolute top-0 start-0 m-3 badge rounded-pill"
                    [ngClass]="item.isVeg ? 'bg-success' : 'bg-danger'">
                {{ item.isVeg ? 'VEG 🌱' : 'NON-VEG 🍗' }}
              </span>
              <span class="position-absolute top-0 end-0 m-3 badge bg-dark opacity-75 rounded-pill">
                <i class="fa-regular fa-clock me-1"></i>{{ item.prepTimeMins }} mins
              </span>
            </div>

            <div class="p-4 d-flex flex-column flex-grow-1">
              <div class="d-flex justify-content-between align-items-start mb-1">
                <h5 class="fw-bold mb-0 text-dark">{{ item.name }}</h5>
              </div>
              
              <!-- Special Highlight Tagline -->
              <div class="mb-2">
                <span class="badge bg-warning-subtle text-dark border border-warning rounded-pill px-2 py-1 extra-small">
                  {{ getSpecialTag(item) }}
                </span>
              </div>

              <p class="text-muted small flex-grow-1 text-truncate-2">{{ item.description }}</p>
              
              <div class="d-flex align-items-center justify-content-between pt-3 border-top mt-2" (click)="$event.stopPropagation()">
                <span class="fs-4 fw-bold text-dark font-serif">₹{{ item.price.toFixed(2) }}</span>
                <div class="d-flex gap-2">
                  <button class="btn btn-outline-dark rounded-pill btn-sm px-3" (click)="openItemModal(item)">
                    <i class="fa-solid fa-eye me-1"></i> Details
                  </button>
                  <button class="btn btn-cafe rounded-pill btn-sm px-3" (click)="addToCart(item)">
                    <i class="fa-solid fa-plus me-1"></i> Add
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Coffee / Item Details Modal Overlay -->
    <div class="modal-backdrop fade show" *ngIf="showDetailModal" (click)="closeItemModal()" style="z-index: 1050; background-color: rgba(0,0,0,0.65); backdrop-filter: blur(4px);"></div>
    
    <div class="modal d-block fade show" *ngIf="showDetailModal" tabindex="-1" style="z-index: 1055;">
      <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content border-0 rounded-4 shadow-lg overflow-hidden" *ngIf="selectedItem">
          
          <!-- Modal Header Image Banner -->
          <div class="position-relative" style="height: 260px; overflow: hidden;">
            <img [src]="selectedItem.imageUrl || defaultFallbackImg" 
                 (error)="onImgError($event)"
                 class="w-100 h-100 object-fit-cover" [alt]="selectedItem.name">
            <div class="position-absolute top-0 start-0 end-0 bottom-0" 
                 style="background: linear-gradient(to top, rgba(0,0,0,0.85) 0%, rgba(0,0,0,0.2) 60%, rgba(0,0,0,0.4) 100%);">
            </div>
            
            <button type="button" class="btn-close btn-close-white position-absolute top-0 end-0 m-3 p-2 bg-dark rounded-circle opacity-100 shadow" (click)="closeItemModal()"></button>

            <!-- Badges overlay -->
            <div class="position-absolute top-0 start-0 m-3 d-flex gap-2 flex-wrap">
              <span class="badge rounded-pill px-3 py-2 fs-6 shadow-sm" [ngClass]="selectedItem.isVeg ? 'bg-success' : 'bg-danger'">
                {{ selectedItem.isVeg ? 'VEG 🌱' : 'NON-VEG 🍗' }}
              </span>
              <span class="badge bg-warning text-dark rounded-pill px-3 py-2 fs-6 fw-bold shadow-sm">
                {{ getSpecialTag(selectedItem) }}
              </span>
            </div>

            <!-- Title & Price Overlay -->
            <div class="position-absolute bottom-0 start-0 end-0 p-4 text-white">
              <div class="d-flex align-items-end justify-content-between">
                <div>
                  <h2 class="font-serif fw-bold mb-1 display-6">{{ selectedItem.name }}</h2>
                  <span class="badge bg-light text-dark rounded-pill px-3 py-1 small">
                    <i class="fa-regular fa-clock me-1"></i> Prep Time: {{ selectedItem.prepTimeMins }} mins
                  </span>
                </div>
                <div class="text-end">
                  <div class="fs-6 text-white-50">Unit Price</div>
                  <div class="fs-2 fw-bold text-warning font-serif">₹{{ selectedItem.price.toFixed(2) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Body Content -->
          <div class="modal-body p-4 bg-white">
            <!-- Description -->
            <div class="mb-4">
              <h6 class="fw-bold text-muted text-uppercase small tracking-wide">Description</h6>
              <p class="fs-6 text-secondary lead-sm mb-0">{{ selectedItem.description }}</p>
            </div>

            <!-- What's Special Feature Callout -->
            <div class="p-3 mb-4 rounded-3 border border-warning-subtle bg-warning-subtle text-dark d-flex align-items-center gap-3">
              <i class="fa-solid fa-wand-magic-sparkles fs-3 text-warning"></i>
              <div>
                <strong class="d-block fw-bold">Speciality Highlight</strong>
                <span class="small">{{ getSpecialTag(selectedItem) }} — Handcrafted premium preparation for an unmatched taste.</span>
              </div>
            </div>

            <!-- Customizations Row -->
            <div class="row g-4 mb-3">
              <!-- Sugar Packets (Shown FOR COFFEE ONLY, NOT FOR DRINKS/FOOD) -->
              <div class="col-md-7" *ngIf="isCoffeeItem(selectedItem)">
                <label class="form-label fw-bold text-dark d-flex align-items-center">
                  <i class="fa-solid fa-cubes-stacked me-2 text-warning"></i> Sugar Packets Quantity:
                </label>
                <div class="d-flex gap-2 flex-wrap">
                  <button *ngFor="let s of [0, 1, 2, 3, 4]" 
                          type="button"
                          class="btn btn-sm rounded-pill px-3 fw-semibold transition-all"
                          [ngClass]="modalSugarPackets === s ? 'btn-cafe shadow-sm' : 'btn-outline-secondary'"
                          (click)="setSugarPackets(s)">
                    {{ s === 0 ? 'No Sugar' : s + ' Packet' + (s > 1 ? 's' : '') }}
                  </button>
                </div>
              </div>

              <!-- Coffee / Item Quantity Counter -->
              <div class="col-md-5" [ngClass]="isCoffeeItem(selectedItem) ? 'col-md-5' : 'col-md-12'">
                <label class="form-label fw-bold text-dark d-flex align-items-center">
                  <i class="fa-solid fa-hashtag me-2 text-primary"></i> Number of Items:
                </label>
                <div class="input-group input-group-lg rounded-pill overflow-hidden shadow-sm" style="max-width: 200px;">
                  <button class="btn btn-outline-secondary px-3" type="button" (click)="decreaseModalQty()">
                    <i class="fa-solid fa-minus"></i>
                  </button>
                  <input type="text" class="form-control text-center fw-bold border-0 bg-light" [value]="modalQuantity" readonly>
                  <button class="btn btn-outline-secondary px-3" type="button" (click)="increaseModalQty()">
                    <i class="fa-solid fa-plus"></i>
                  </button>
                </div>
              </div>
            </div>

            <!-- Optional Special Request Notes -->
            <div class="mt-3">
              <label for="itemNotes" class="form-label fw-semibold text-muted small">Special Instructions / Customization:</label>
              <input type="text" id="itemNotes" class="form-control rounded-3 py-2" placeholder="e.g. Extra hot, extra cheese, less spicy..." [(ngModel)]="modalNotes">
            </div>
          </div>

          <!-- Modal Footer with Dynamic Total & Add To Cart Button -->
          <div class="modal-footer bg-light p-4 d-flex align-items-center justify-content-between border-top">
            <div>
              <span class="text-muted small d-block">Total Price</span>
              <span class="fs-3 fw-bold text-dark font-serif">₹{{ (selectedItem.price * modalQuantity).toFixed(2) }}</span>
            </div>
            
            <div class="d-flex gap-2">
              <button type="button" class="btn btn-outline-secondary rounded-pill px-4" (click)="closeItemModal()">
                Cancel
              </button>
              <button type="button" class="btn btn-cafe rounded-pill px-4 py-2 fw-bold shadow" (click)="addToCartFromModal()">
                <i class="fa-solid fa-cart-plus me-2"></i> Add to Cart (₹{{ (selectedItem.price * modalQuantity).toFixed(2) }})
              </button>
            </div>
          </div>

        </div>
      </div>
    </div>

    <!-- Floating Cart Drawer Sidebar -->
    <div class="offcanvas offcanvas-end" [ngClass]="{ 'show': showCart }" tabindex="-1" style="visibility: visible;" *ngIf="showCart">
      <div class="offcanvas-header bg-dark text-white">
        <h5 class="offcanvas-title font-serif fw-bold"><i class="fa-solid fa-basket-shopping me-2"></i> Table #{{ tableNumber }} Cart</h5>
        <button type="button" class="btn-close btn-close-white" (click)="toggleCartDrawer()"></button>
      </div>
      <div class="offcanvas-body d-flex flex-column">
        <div *ngIf="!cart?.items?.length" class="text-center py-5 my-auto text-muted">
          <i class="fa-solid fa-basket-shopping display-1 mb-3 text-light"></i>
          <h6>Your cart is empty</h6>
          <p class="small">Add some delicious dishes from our digital menu!</p>
        </div>

        <div *ngIf="cart?.items?.length" class="flex-grow-1 overflow-auto pe-2">
          <div *ngFor="let ci of cart?.items" class="card border-0 shadow-sm rounded-4 mb-3 p-3">
            <div class="d-flex align-items-center gap-3">
              <img [src]="ci.menuItem.imageUrl" class="rounded-3 object-fit-cover" style="width: 60px; height: 60px;">
              <div class="flex-grow-1">
                <h6 class="fw-bold mb-0">{{ ci.menuItem.name }}</h6>
                <div class="text-muted small">₹{{ ci.menuItem.price.toFixed(2) }} each</div>
                <div class="text-secondary extra-small font-monospace text-muted mt-1" *ngIf="ci.notes">
                  <i class="fa-solid fa-pen-to-square me-1"></i> {{ ci.notes }}
                </div>
                <input type="text" class="form-control form-control-sm mt-1" placeholder="Item note (e.g. Extra hot)" 
                       [(ngModel)]="ci.notes" (change)="updateItemQuantity(ci.id!, ci.quantity)">
              </div>
            </div>
            <div class="d-flex align-items-center justify-content-between mt-3 pt-2 border-top">
              <div class="btn-group btn-group-sm">
                <button class="btn btn-outline-secondary" (click)="updateItemQuantity(ci.id!, ci.quantity - 1)">-</button>
                <span class="btn btn-light disabled text-dark fw-bold">{{ ci.quantity }}</span>
                <button class="btn btn-outline-secondary" (click)="updateItemQuantity(ci.id!, ci.quantity + 1)">+</button>
              </div>
              <span class="fw-bold">₹{{ (ci.menuItem.price * ci.quantity).toFixed(2) }}</span>
            </div>
          </div>
        </div>

        <div *ngIf="cart?.items?.length" class="pt-3 border-top mt-auto">
          <div class="d-flex justify-content-between mb-2">
            <span class="text-muted">Subtotal:</span>
            <span class="fw-bold fs-5 font-serif">₹{{ cartSubtotal.toFixed(2) }}</span>
          </div>

          <div class="form-floating mb-3">
            <input type="text" class="form-control rounded-3" id="custName" placeholder="Your Name" [(ngModel)]="customerName">
            <label for="custName">Your Name (Optional)</label>
          </div>

          <button class="btn btn-cafe w-100 py-3 rounded-pill fw-bold" (click)="placeOrder()">
            <i class="fa-solid fa-paper-plane me-2"></i> Place Order Now
          </button>
        </div>
      </div>
    </div>
  `
})
export class MenuComponent implements OnInit {
  tableNumber = 1;
  tableId = 1;
  sessionId = '';
  categories: Category[] = [];
  menuItems: MenuItem[] = [];
  filteredMenuItems: MenuItem[] = [];
  selectedCategoryId: number | null = null;
  searchQuery = '';
  foodTypeFilter: 'ALL' | 'VEG' | 'NON_VEG' = 'ALL';

  cart: Cart | null = null;
  showCart = false;
  customerName = '';
  activeOrder: Order | null = null;
  tableStatus = 'AVAILABLE';
  isTableOccupiedByOther = false;

  // Detail Modal State
  selectedItem: MenuItem | null = null;
  modalQuantity = 1;
  modalSugarPackets = 0;
  modalNotes = '';
  showDetailModal = false;
  defaultFallbackImg = 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80';

  onImgError(event: any): void {
    event.target.src = this.defaultFallbackImg;
  }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private customerService: CustomerService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (!params['table']) {
        this.toastService.show('Please scan a Table QR Code or select an available table first!', 'info');
        this.router.navigate(['/customer/tables']);
        return;
      }
      this.tableNumber = parseInt(params['table']);
      this.initSession();
    });
  }

  initSession(): void {
    let session = sessionStorage.getItem('cafe_session');
    if (!session) {
      session = 'SESS-' + Math.random().toString(36).substring(2, 9);
      sessionStorage.setItem('cafe_session', session);
    }
    this.sessionId = session;

    // Load menu items & categories immediately
    this.loadCategories();
    this.loadMenuItems();

    // Get Table info
    this.customerService.getTableInfo(this.tableNumber).subscribe({
      next: (tbl) => {
        this.tableId = tbl.id;
        this.tableStatus = tbl.status;

        // Auto-occupy table for guest when scanning QR code via Google Lens if AVAILABLE
        if (tbl.status === 'AVAILABLE') {
          sessionStorage.setItem('my_occupied_table', tbl.tableNumber.toString());
          this.customerService.occupyTable(tbl.id).subscribe({
            next: (updatedTbl) => {
              this.tableStatus = updatedTbl.status;
              this.loadCart();
              this.checkActiveOrder();
            },
            error: () => {
              this.loadCart();
              this.checkActiveOrder();
            }
          });
        } else {
          this.loadCart();
          this.checkActiveOrder();
        }
      },
      error: () => {
        this.tableId = 1;
      }
    });
  }

  loadCategories(): void {
    this.customerService.getCategories().subscribe(res => this.categories = res);
  }

  loadMenuItems(): void {
    this.customerService.getMenuItems().subscribe(res => {
      this.menuItems = res;
      this.filterMenu();
    });
  }

  loadCart(): void {
    this.customerService.getCart(this.sessionId, this.tableId).subscribe(res => this.cart = res);
  }

  checkActiveOrder(): void {
    const myOccupiedTable = sessionStorage.getItem('my_occupied_table');
    const isMyTable = myOccupiedTable === this.tableNumber.toString();

    this.customerService.getActiveOrderByTable(this.tableId).subscribe({
      next: (order) => {
        if (order && (order.sessionId === this.sessionId || isMyTable)) {
          // Current guest owns this active order!
          this.activeOrder = order;
          this.isTableOccupiedByOther = false;
        } else if (order) {
          // Active order belongs to another guest!
          this.activeOrder = null;
          this.isTableOccupiedByOther = true;
        } else if ((this.tableStatus === 'OCCUPIED' || this.tableStatus === 'BILL_REQUESTED') && !isMyTable) {
          this.activeOrder = null;
          this.isTableOccupiedByOther = true;
        } else {
          this.activeOrder = null;
          this.isTableOccupiedByOther = false;
        }
      },
      error: () => {
        this.activeOrder = null;
        if ((this.tableStatus === 'OCCUPIED' || this.tableStatus === 'BILL_REQUESTED') && !isMyTable) {
          this.isTableOccupiedByOther = true;
        } else {
          this.isTableOccupiedByOther = false;
        }
      }
    });
  }

  selectCategory(catId: number | null): void {
    this.selectedCategoryId = catId;
    this.filterMenu();
  }

  setFoodTypeFilter(filter: 'ALL' | 'VEG' | 'NON_VEG'): void {
    this.foodTypeFilter = filter;
    this.filterMenu();
  }

  filterMenu(): void {
    this.filteredMenuItems = this.menuItems.filter(item => {
      const itemCatId = item.categoryId || item.category?.id;
      const matchCat = this.selectedCategoryId === null || itemCatId === this.selectedCategoryId;
      const matchSearch = !this.searchQuery || item.name.toLowerCase().includes(this.searchQuery.toLowerCase()) || (item.description && item.description.toLowerCase().includes(this.searchQuery.toLowerCase()));
      
      let matchDiet = true;
      if (this.foodTypeFilter === 'VEG') {
        matchDiet = item.isVeg === true;
      } else if (this.foodTypeFilter === 'NON_VEG') {
        matchDiet = item.isVeg === false;
      }

      return matchCat && matchSearch && matchDiet;
    });
  }

  // Modal Handlers
  openItemModal(item: MenuItem): void {
    this.selectedItem = item;
    this.modalQuantity = 1;
    this.modalSugarPackets = 0;
    this.modalNotes = '';
    this.showDetailModal = true;
  }

  closeItemModal(): void {
    this.showDetailModal = false;
    this.selectedItem = null;
  }

  isCoffeeItem(item: MenuItem | null): boolean {
    if (!item) return false;
    const catName = item.category?.name?.toLowerCase() || '';
    const name = item.name.toLowerCase();
    // Only show sugar packets for Coffee & Tea drinks, NOT for milkshakes, mocktails, juices, pizzas, burgers, shawarmas
    if (catName.includes('shake') || catName.includes('mocktail') || catName.includes('juice') || catName.includes('pizza') || catName.includes('burger') || catName.includes('shawarma')) return false;
    return catName.includes('coffee') || catName.includes('espresso') || catName.includes('tea') || 
           name.includes('espresso') || name.includes('cappuccino') || name.includes('latte') || 
           name.includes('americano') || name.includes('mocha') || name.includes('macchiato') || 
           name.includes('chai') || name.includes('coffee');
  }

  getSpecialTag(item: MenuItem | null): string {
    if (!item) return '';
    const name = item.name.toLowerCase();
    const catName = item.category?.name?.toLowerCase() || '';

    if (catName.includes('pizza') || name.includes('pizza')) return item.isVeg ? '🍕 Wood-Fired Veg Cheese' : '🍕 Gourmet Stone-Baked Meat';
    if (catName.includes('burger') || name.includes('burger')) return item.isVeg ? '🍔 Crispy Paneer Brioche' : '🍔 Juicy Angus Chicken/Beef';
    if (catName.includes('shawarma') || name.includes('shawarma') || name.includes('wrap') || name.includes('roll')) return item.isVeg ? '🌯 Fresh Paneer Kathi Roll' : '🌯 Authentic Toum Garlic Shawarma';

    if (catName.includes('shake') || name.includes('shake') || name.includes('thickshake')) return '🧋 Creamy & Thick Gelato';
    if (catName.includes('mocktail') || name.includes('mojito') || name.includes('fizz') || name.includes('sangria') || name.includes('splash')) return '🍹 Refreshing Craft Sparkler';
    if (catName.includes('juice') || name.includes('juice') || name.includes('detox') || name.includes('orange')) return '🍎 100% Raw Cold-Pressed';

    if (name.includes('espresso')) return '☕ 100% Arabica Shot';
    if (name.includes('cappuccino')) return '✨ Salted Foam Special';
    if (name.includes('latte')) return '🥛 Silky Vanilla Infused';
    if (name.includes('matcha')) return '🍵 Ceremonial Uji Grade';
    if (name.includes('toast') || name.includes('avocado')) return '🥑 Organic Sourdough';
    if (name.includes('pancake')) return '🥞 Triple Stack Fluffy';
    if (name.includes('cake') || name.includes('chocolate')) return '🍫 Belgian Chocolate';
    return item.isVeg ? '🌱 House Artisan Special' : '⭐ Chef Signature Selection';
  }

  increaseModalQty(): void {
    this.modalQuantity++;
  }

  decreaseModalQty(): void {
    if (this.modalQuantity > 1) {
      this.modalQuantity--;
    }
  }

  setSugarPackets(count: number): void {
    this.modalSugarPackets = count;
  }

  addToCartFromModal(): void {
    if (!this.selectedItem) return;

    if (this.isTableOccupiedByOther) {
      this.toastService.show(`Table #${this.tableNumber} is currently occupied by another customer. Please select a free table!`, 'error');
      return;
    }

    let notesArr: string[] = [];
    if (this.isCoffeeItem(this.selectedItem)) {
      notesArr.push(`${this.modalSugarPackets} Sugar Packet${this.modalSugarPackets === 1 ? '' : 's'}`);
    }
    if (this.modalNotes.trim()) {
      notesArr.push(this.modalNotes.trim());
    }
    const fullNotes = notesArr.join(' | ');

    this.customerService.addToCart(this.sessionId, this.tableId, this.selectedItem.id, this.modalQuantity, fullNotes).subscribe({
      next: (c) => {
        this.cart = c;
        this.toastService.show(`Added ${this.modalQuantity}x ${this.selectedItem?.name} to Cart!`, 'success');
        this.closeItemModal();
      },
      error: () => this.toastService.show('Failed to add item', 'error')
    });
  }

  addToCart(item: MenuItem): void {
    if (this.isTableOccupiedByOther) {
      this.toastService.show(`Table #${this.tableNumber} is currently occupied by another customer. Please select a free table!`, 'error');
      return;
    }

    this.customerService.addToCart(this.sessionId, this.tableId, item.id, 1).subscribe({
      next: (c) => {
        this.cart = c;
        this.toastService.show(`Added ${item.name} to Cart!`, 'success');
      },
      error: () => this.toastService.show('Failed to add item', 'error')
    });
  }

  updateItemQuantity(cartItemId: number, qty: number): void {
    this.customerService.updateCartItem(cartItemId, qty).subscribe(c => this.cart = c);
  }

  toggleCartDrawer(): void {
    this.showCart = !this.showCart;
  }

  get cartCount(): number {
    return this.cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;
  }

  get cartSubtotal(): number {
    return this.cart?.items?.reduce((sum, item) => sum + (item.menuItem.price * item.quantity), 0) || 0;
  }

  placeOrder(): void {
    if (!this.cart?.items?.length) return;

    const payload = {
      sessionId: this.sessionId,
      tableId: this.tableId,
      customerName: this.customerName || `Table ${this.tableNumber} Guest`,
      items: this.cart.items.map(ci => ({
        menuItemId: ci.menuItem.id,
        quantity: ci.quantity,
        notes: ci.notes
      }))
    };

    this.customerService.placeOrder(payload).subscribe({
      next: (order) => {
        this.activeOrder = order;
        this.showCart = false;
        this.loadCart();
        this.toastService.show(`Order #${order.orderNumber} sent to kitchen!`, 'success');
      },
      error: (err) => this.toastService.show(err.error?.message || 'Error placing order', 'error')
    });
  }

  onRequestBill(): void {
    this.customerService.requestBill(this.tableId).subscribe({
      next: () => this.toastService.show('Bill request sent to cashier/waiter!', 'info')
    });
  }
}
