import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, MenuItem, RestaurantTable, Coupon, Inventory, StockHistory, DashboardSummary, User } from '../models/cafe.models';

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = '/api/admin';

  constructor(private http: HttpClient) {}

  getDashboardSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/dashboard/summary`);
  }

  exportSalesReportExcel(): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/reports/sales/excel`, { responseType: 'blob' }) as Observable<Blob>;
  }

  // Categories
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories`);
  }

  createCategory(cat: Partial<Category>): Observable<Category> {
    return this.http.post<Category>(`${this.apiUrl}/categories`, cat);
  }

  updateCategory(id: number, cat: Partial<Category>): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/categories/${id}`, cat);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/categories/${id}`);
  }

  // Menu Items
  getMenuItems(): Observable<MenuItem[]> {
    return this.http.get<MenuItem[]>(`${this.apiUrl}/menu`);
  }

  createMenuItem(item: Partial<MenuItem>, categoryId: number): Observable<MenuItem> {
    return this.http.post<MenuItem>(`${this.apiUrl}/menu?categoryId=${categoryId}`, item);
  }

  updateMenuItem(id: number, item: Partial<MenuItem>, categoryId?: number): Observable<MenuItem> {
    let url = `${this.apiUrl}/menu/${id}`;
    if (categoryId) url += `?categoryId=${categoryId}`;
    return this.http.put<MenuItem>(url, item);
  }

  toggleMenuItemAvailability(id: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/menu/${id}/toggle-availability`, {});
  }

  deleteMenuItem(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/menu/${id}`);
  }

  // Tables & QR
  getTables(): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.apiUrl}/tables`);
  }

  createTable(tableNumber: number, capacity: number): Observable<RestaurantTable> {
    return this.http.post<RestaurantTable>(`${this.apiUrl}/tables?tableNumber=${tableNumber}&capacity=${capacity}`, {});
  }

  getTableQr(tableId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/tables/${tableId}/qr`);
  }

  // Employees
  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  createEmployee(employeeData: any): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/users`, employeeData);
  }

  // Inventory
  getInventory(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(`${this.apiUrl}/inventory`);
  }

  getLowStockAlerts(): Observable<Inventory[]> {
    return this.http.get<Inventory[]>(`${this.apiUrl}/inventory/low-stock`);
  }

  createInventoryItem(item: Partial<Inventory>): Observable<Inventory> {
    return this.http.post<Inventory>(`${this.apiUrl}/inventory`, item);
  }

  updateStock(id: number, quantity: number, transactionType: string, notes?: string): Observable<Inventory> {
    let url = `${this.apiUrl}/inventory/${id}/stock?quantity=${quantity}&transactionType=${transactionType}`;
    if (notes) url += `&notes=${encodeURIComponent(notes)}`;
    return this.http.post<Inventory>(url, {});
  }

  getStockHistory(inventoryId: number): Observable<StockHistory[]> {
    return this.http.get<StockHistory[]>(`${this.apiUrl}/inventory/${inventoryId}/history`);
  }

  // Coupons
  getCoupons(): Observable<Coupon[]> {
    return this.http.get<Coupon[]>(`${this.apiUrl}/coupons`);
  }

  createCoupon(coupon: Partial<Coupon>): Observable<Coupon> {
    return this.http.post<Coupon>(`${this.apiUrl}/coupons`, coupon);
  }

  updateCoupon(id: number, coupon: Partial<Coupon>): Observable<Coupon> {
    return this.http.put<Coupon>(`${this.apiUrl}/coupons/${id}`, coupon);
  }

  deleteCoupon(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/coupons/${id}`);
  }

  // Audit Logs
  getAuditLogs(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/audit-logs`);
  }
}
