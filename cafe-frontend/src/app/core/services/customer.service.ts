import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Category, MenuItem, RestaurantTable, Cart, Order, Invoice } from '../models/cafe.models';

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  private apiUrl = 'http://localhost:8080/api/customer';

  constructor(private http: HttpClient) {}

  getTables(): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.apiUrl}/tables`);
  }

  getTableInfo(tableNumber: number): Observable<RestaurantTable> {
    return this.http.get<RestaurantTable>(`${this.apiUrl}/table/${tableNumber}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/categories`);
  }

  getMenuItems(categoryId?: number, search?: string): Observable<MenuItem[]> {
    let url = `${this.apiUrl}/menu`;
    const params: string[] = [];
    if (categoryId) params.push(`categoryId=${categoryId}`);
    if (search) params.push(`search=${encodeURIComponent(search)}`);
    if (params.length) url += `?${params.join('&')}`;
    return this.http.get<MenuItem[]>(url);
  }

  getCart(sessionId: string, tableId: number): Observable<Cart> {
    return this.http.get<Cart>(`${this.apiUrl}/cart?sessionId=${sessionId}&tableId=${tableId}`);
  }

  addToCart(sessionId: string, tableId: number, menuItemId: number, quantity: number = 1, notes?: string): Observable<Cart> {
    let url = `${this.apiUrl}/cart/add?sessionId=${sessionId}&tableId=${tableId}&menuItemId=${menuItemId}&quantity=${quantity}`;
    if (notes) url += `&notes=${encodeURIComponent(notes)}`;
    return this.http.post<Cart>(url, {});
  }

  updateCartItem(cartItemId: number, quantity: number): Observable<Cart> {
    return this.http.put<Cart>(`${this.apiUrl}/cart/items/${cartItemId}?quantity=${quantity}`, {});
  }

  clearCart(sessionId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/cart/clear?sessionId=${sessionId}`);
  }

  placeOrder(orderData: any): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/order/place`, orderData);
  }

  getOrderStatus(orderId: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/order/${orderId}/status`);
  }

  getActiveOrderByTable(tableId: number): Observable<Order> {
    return this.http.get<Order>(`${this.apiUrl}/order/table/${tableId}/active`);
  }

  occupyTable(tableId: number, sessionId?: string, customerTokenSerial?: string): Observable<RestaurantTable> {
    let params: any = {};
    if (sessionId) params.sessionId = sessionId;
    if (customerTokenSerial) params.customerTokenSerial = customerTokenSerial;
    return this.http.post<RestaurantTable>(`${this.apiUrl}/table/${tableId}/occupy`, {}, { params });
  }

  switchTable(sessionId: string, fromTableId: number, toTableId: number, customerTokenSerial: string): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/table/switch`, {
      sessionId,
      fromTableId,
      toTableId,
      customerTokenSerial
    });
  }

  requestBill(tableId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/table/${tableId}/request-bill`, {});
  }

  getInvoice(orderId: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/invoice/order/${orderId}`);
  }

  payInvoice(orderId: number, paymentMethod: string, transactionRef?: string): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/invoice/pay`, {
      orderId,
      paymentMethod,
      transactionRef
    });
  }

  downloadInvoicePdf(orderId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/invoice/order/${orderId}/pdf`, { responseType: 'blob' }) as Observable<Blob>;
  }
}
