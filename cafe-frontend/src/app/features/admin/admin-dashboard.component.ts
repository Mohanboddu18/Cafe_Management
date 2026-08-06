import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';
import { ToastService } from '../../core/services/toast.service';
import { Category, MenuItem, RestaurantTable, Coupon, Inventory, DashboardSummary, User } from '../../core/models/cafe.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container-fluid py-4 px-4">
      <!-- Admin Top Bar -->
      <div class="d-flex align-items-center justify-content-between mb-4 bg-dark text-white p-3 rounded-4 shadow-sm">
        <div class="d-flex align-items-center gap-3">
          <div class="bg-warning text-dark rounded-circle p-3 d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
            <i class="fa-solid fa-chart-line fs-3"></i>
          </div>
          <div>
            <h3 class="font-serif fw-bold mb-0">Admin Management Portal</h3>
            <span class="text-white-50 small">System Dashboard, Menu CRUD, QR Generation, Inventory & Reports</span>
          </div>
        </div>
        <button class="btn btn-warning text-dark fw-bold rounded-pill px-4" (click)="exportExcel()">
          <i class="fa-solid fa-file-excel me-1"></i> Export Sales Report (.xlsx)
        </button>
      </div>

      <!-- Stat Cards Row -->
      <div class="row g-4 mb-4" *ngIf="summary">
        <div class="col-md-3">
          <div class="glass-card p-4 border-start border-4 border-success">
            <div class="text-muted small fw-bold">TODAY'S REVENUE</div>
            <h2 class="font-serif fw-bold text-success mb-0">₹{{ summary.todaysSales.toFixed(2) }}</h2>
          </div>
        </div>
        <div class="col-md-3">
          <div class="glass-card p-4 border-start border-4 border-warning">
            <div class="text-muted small fw-bold">WEEKLY REVENUE</div>
            <h2 class="font-serif fw-bold text-warning mb-0">₹{{ summary.weeklySales.toFixed(2) }}</h2>
          </div>
        </div>
        <div class="col-md-3">
          <div class="glass-card p-4 border-start border-4 border-primary">
            <div class="text-muted small fw-bold">ACTIVE ORDERS</div>
            <h2 class="font-serif fw-bold text-primary mb-0">{{ summary.activeOrdersCount }}</h2>
          </div>
        </div>
        <div class="col-md-3">
          <div class="glass-card p-4 border-start border-4 border-info">
            <div class="text-muted small fw-bold">TABLE OCCUPANCY</div>
            <h2 class="font-serif fw-bold text-info mb-0">{{ summary.occupiedTablesCount }} / {{ summary.totalTablesCount }}</h2>
          </div>
        </div>
      </div>

      <!-- Navigation Tabs -->
      <ul class="nav nav-pills mb-4 gap-2 border-bottom pb-3">
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'dashboard' }" (click)="activeTab = 'dashboard'">
            <i class="fa-solid fa-gauge me-1"></i> Overview
          </button>
        </li>
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'categories' }" (click)="activeTab = 'categories'; loadCategories()">
            <i class="fa-solid fa-layer-group me-1"></i> Categories
          </button>
        </li>
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'menu' }" (click)="activeTab = 'menu'; loadMenuItems()">
            <i class="fa-solid fa-utensils me-1"></i> Menu Items
          </button>
        </li>
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'tables' }" (click)="activeTab = 'tables'; loadTables()">
            <i class="fa-solid fa-qrcode me-1"></i> Tables & QR
          </button>
        </li>
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'employees' }" (click)="activeTab = 'employees'; loadUsers()">
            <i class="fa-solid fa-users me-1"></i> Staff Members
          </button>
        </li>
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'inventory' }" (click)="activeTab = 'inventory'; loadInventory()">
            <i class="fa-solid fa-boxes-stacked me-1"></i> Inventory
          </button>
        </li>
        <li class="nav-item">
          <button class="nav-link rounded-pill px-4 fw-semibold" [ngClass]="{ 'active bg-dark': activeTab === 'coupons' }" (click)="activeTab = 'coupons'; loadCoupons()">
            <i class="fa-solid fa-ticket me-1"></i> Coupons
          </button>
        </li>
      </ul>

      <!-- TAB 1: OVERVIEW -->
      <div *ngIf="activeTab === 'dashboard'" class="row g-4">
        <div class="col-lg-7">
          <div class="glass-card p-4">
            <h5 class="font-serif fw-bold mb-3">7-Day Sales Trend</h5>
            <div class="d-flex align-items-end gap-3 pt-4 px-2" style="height: 220px;" *ngIf="summary">
              <div *ngFor="let entry of summary.dailyRevenueChart | keyvalue" class="flex-grow-1 text-center">
                <div class="bg-warning rounded-top" [style.height.px]="(entry.value / 100) * 15 + 20" style="min-height: 20px;"></div>
                <div class="small fw-bold mt-2 text-truncate">{{ entry.key }}</div>
                <div class="small text-muted">₹{{ entry.value.toFixed(0) }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="col-lg-5">
          <div class="glass-card p-4">
            <h5 class="font-serif fw-bold mb-3"><i class="fa-solid fa-crown text-warning me-2"></i> Popular Menu Items</h5>
            <div class="list-group list-group-flush" *ngIf="summary">
              <div *ngFor="let item of summary.popularItems" class="list-group-item d-flex justify-content-between align-items-center bg-transparent py-2">
                <span class="fw-bold">{{ item.itemName }}</span>
                <span class="badge bg-warning text-dark rounded-pill">{{ item.quantitySold }} orders</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 2: CATEGORIES MANAGEMENT -->
      <div *ngIf="activeTab === 'categories'">
        <div class="d-flex justify-content-between mb-3">
          <h4 class="font-serif fw-bold">Category Management</h4>
          <button class="btn btn-cafe rounded-pill btn-sm px-3" (click)="openCategoryModal()">
            <i class="fa-solid fa-plus me-1"></i> Add Category
          </button>
        </div>
        <div class="row g-4">
          <div class="col-md-4" *ngFor="let cat of categories">
            <div class="glass-card p-3 d-flex align-items-center gap-3">
              <img [src]="cat.imageUrl" class="rounded-3 object-fit-cover" style="width: 70px; height: 70px;">
              <div class="flex-grow-1">
                <h6 class="fw-bold mb-1">{{ cat.name }}</h6>
                <div class="text-muted small">{{ cat.description }}</div>
              </div>
              <button class="btn btn-outline-danger btn-sm rounded-circle" (click)="deleteCategory(cat.id)">
                <i class="fa-solid fa-trash"></i>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 3: MENU MANAGEMENT -->
      <div *ngIf="activeTab === 'menu'">
        <div class="d-flex justify-content-between mb-3">
          <h4 class="font-serif fw-bold">Menu Management</h4>
          <button class="btn btn-cafe rounded-pill btn-sm px-3" (click)="openMenuModal()">
            <i class="fa-solid fa-plus me-1"></i> Add Menu Item
          </button>
        </div>
        <div class="table-responsive glass-card p-3">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Item</th>
                <th>Category</th>
                <th>Price</th>
                <th>Veg / Non-Veg</th>
                <th>Availability</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of menuItems">
                <td class="d-flex align-items-center gap-3">
                  <img [src]="item.imageUrl" class="rounded-3 object-fit-cover" style="width: 45px; height: 45px;">
                  <span class="fw-bold">{{ item.name }}</span>
                </td>
                <td>{{ item.category?.name || 'Category ' + item.categoryId }}</td>
                <td class="fw-bold">₹{{ item.price.toFixed(2) }}</td>
                <td>
                  <span class="badge" [ngClass]="item.isVeg ? 'bg-success' : 'bg-danger'">
                    {{ item.isVeg ? 'VEG' : 'NON-VEG' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-sm rounded-pill px-3"
                          [ngClass]="item.isAvailable ? 'btn-outline-success' : 'btn-outline-danger'"
                          (click)="toggleAvailability(item.id)">
                    {{ item.isAvailable ? 'AVAILABLE' : 'OUT OF STOCK' }}
                  </button>
                </td>
                <td>
                  <button class="btn btn-outline-danger btn-sm rounded-circle ms-2" (click)="deleteMenuItem(item.id)">
                    <i class="fa-solid fa-trash"></i>
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 4: TABLES & QR CODE -->
      <div *ngIf="activeTab === 'tables'">
        <div class="d-flex justify-content-between mb-3">
          <h4 class="font-serif fw-bold">Table & QR Code Management</h4>
          <button class="btn btn-cafe rounded-pill btn-sm px-3" (click)="createTable()">
            <i class="fa-solid fa-plus me-1"></i> Add New Table
          </button>
        </div>
        <div class="row g-4">
          <div class="col-md-3" *ngFor="let tbl of tables">
            <div class="glass-card p-3 text-center">
              <span class="badge bg-dark mb-2">TABLE #{{ tbl.tableNumber }}</span>
              <h5 class="fw-bold">Capacity: {{ tbl.capacity }} Persons</h5>
              <div class="p-2 bg-white rounded-3 border my-2" *ngIf="selectedQr[tbl.id]">
                <img [src]="selectedQr[tbl.id]" class="img-fluid" style="max-height: 140px;">
              </div>
              <button class="btn btn-outline-dark btn-sm rounded-pill w-100 mt-2" (click)="loadQr(tbl.id)">
                <i class="fa-solid fa-qrcode me-1"></i> View / Print QR Code
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 5: STAFF MEMBERS -->
      <div *ngIf="activeTab === 'employees'">
        <div class="d-flex justify-content-between mb-3">
          <h4 class="font-serif fw-bold">Staff Members Management</h4>
          <button class="btn btn-cafe rounded-pill btn-sm px-3" (click)="openStaffModal()">
            <i class="fa-solid fa-user-plus me-1"></i> Register Staff Member
          </button>
        </div>
        <div class="table-responsive glass-card p-3">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Username</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Role</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let u of users">
                <td class="fw-bold">{{ u.username }}</td>
                <td>{{ u.fullName }}</td>
                <td>{{ u.email }}</td>
                <td><span class="badge bg-warning text-dark">{{ u.role }}</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 6: INVENTORY -->
      <div *ngIf="activeTab === 'inventory'">
        <h4 class="font-serif fw-bold mb-3">Inventory Stock Control</h4>
        <div class="row g-4">
          <div class="col-md-4" *ngFor="let item of inventory">
            <div class="glass-card p-3 border-start border-4" [ngClass]="item.currentStock <= item.minRequiredStock ? 'border-danger' : 'border-success'">
              <div class="d-flex justify-content-between fw-bold mb-1">
                <span>{{ item.itemName }}</span>
                <span class="badge bg-secondary">{{ item.unit }}</span>
              </div>
              <h3 class="font-serif fw-bold mb-1">{{ item.currentStock }} {{ item.unit }}</h3>
              <div class="small text-muted mb-2">Min Required: {{ item.minRequiredStock }} {{ item.unit }}</div>
              <div *ngIf="item.currentStock <= item.minRequiredStock" class="badge bg-danger text-white mb-2">
                <i class="fa-solid fa-triangle-exclamation me-1"></i> Low Stock Alert!
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- TAB 7: COUPONS -->
      <div *ngIf="activeTab === 'coupons'">
        <div class="d-flex justify-content-between mb-3">
          <h4 class="font-serif fw-bold">Coupons & Promo Codes</h4>
        </div>
        <div class="row g-4">
          <div class="col-md-4" *ngFor="let c of coupons">
            <div class="glass-card p-3 border-start border-4 border-warning">
              <span class="badge bg-dark font-monospace fs-6 mb-2">{{ c.code }}</span>
              <h5 class="fw-bold mb-1">{{ c.description }}</h5>
              <div class="text-muted small mb-2">
                Discount: {{ c.discountValue }}{{ c.discountType === 'PERCENTAGE' ? '%' : '\$' }} OFF
              </div>
              <div class="small text-secondary">Valid Until: {{ c.validUntil }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class AdminDashboardComponent implements OnInit {
  activeTab = 'dashboard';
  summary: DashboardSummary | null = null;
  categories: Category[] = [];
  menuItems: MenuItem[] = [];
  tables: RestaurantTable[] = [];
  selectedQr: { [key: number]: string } = {};
  users: User[] = [];
  inventory: Inventory[] = [];
  coupons: Coupon[] = [];

  constructor(
    private adminService: AdminService,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.loadSummary();
  }

  loadSummary(): void {
    this.adminService.getDashboardSummary().subscribe(res => this.summary = res);
  }

  exportExcel(): void {
    this.adminService.exportSalesReportExcel().subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'Cafe_Sales_Report.xlsx';
      a.click();
      this.toastService.show('Sales report downloaded successfully!', 'success');
    });
  }

  loadCategories(): void {
    this.adminService.getCategories().subscribe(res => this.categories = res);
  }

  loadMenuItems(): void {
    this.adminService.getMenuItems().subscribe(res => this.menuItems = res);
  }

  loadTables(): void {
    this.adminService.getTables().subscribe(res => this.tables = res);
  }

  loadQr(tableId: number): void {
    this.adminService.getTableQr(tableId).subscribe(res => {
      this.selectedQr[tableId] = res.imageBase64;
    });
  }

  createTable(): void {
    const tableNum = this.tables.length + 1;
    this.adminService.createTable(tableNum, 4).subscribe({
      next: () => {
        this.toastService.show(`Table ${tableNum} created with QR!`, 'success');
        this.loadTables();
      }
    });
  }

  toggleAvailability(id: number): void {
    this.adminService.toggleMenuItemAvailability(id).subscribe({
      next: () => this.loadMenuItems()
    });
  }

  deleteMenuItem(id: number): void {
    this.adminService.deleteMenuItem(id).subscribe({
      next: () => {
        this.toastService.show('Menu item deleted', 'info');
        this.loadMenuItems();
      }
    });
  }

  deleteCategory(id: number): void {
    this.adminService.deleteCategory(id).subscribe({
      next: () => {
        this.toastService.show('Category deleted', 'info');
        this.loadCategories();
      }
    });
  }

  loadUsers(): void {
    this.adminService.getUsers().subscribe(res => this.users = res);
  }

  loadInventory(): void {
    this.adminService.getInventory().subscribe(res => this.inventory = res);
  }

  loadCoupons(): void {
    this.adminService.getCoupons().subscribe(res => this.coupons = res);
  }

  openCategoryModal(): void {
    const name = prompt('Enter Category Name:');
    if (name) {
      this.adminService.createCategory({ name, description: 'Fresh artisanal category', active: true, displayOrder: this.categories.length + 1 }).subscribe(() => this.loadCategories());
    }
  }

  openMenuModal(): void {
    const name = prompt('Enter Item Name:');
    const priceStr = prompt('Enter Item Price ($):');
    if (name && priceStr) {
      this.adminService.createMenuItem({
        name,
        description: 'Chef recommendation item',
        price: parseFloat(priceStr),
        imageUrl: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=600&q=80',
        prepTimeMins: 15,
        isVeg: true,
        isAvailable: true
      }, 1).subscribe(() => this.loadMenuItems());
    }
  }

  openStaffModal(): void {
    const username = prompt('Enter Username:');
    const fullName = prompt('Enter Full Name:');
    const email = prompt('Enter Email:');
    const password = prompt('Enter Password:');
    const roleName = prompt('Enter Role (ROLE_WAITER, ROLE_KITCHEN, ROLE_CASHIER):');

    if (username && fullName && email && password && roleName) {
      this.adminService.createEmployee({ username, fullName, email, password, roleName }).subscribe({
        next: () => {
          this.toastService.show('Staff member registered!', 'success');
          this.loadUsers();
        }
      });
    }
  }
}
