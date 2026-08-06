import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestaurantTable, Order, NotificationMsg } from '../models/cafe.models';

@Injectable({
  providedIn: 'root'
})
export class WaiterService {
  private apiUrl = 'http://localhost:8080/api/waiter';

  constructor(private http: HttpClient) {}

  getAllTables(): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.apiUrl}/tables`);
  }

  updateTableStatus(tableId: number, status: string): Observable<RestaurantTable> {
    return this.http.put<RestaurantTable>(`${this.apiUrl}/tables/${tableId}/status?status=${status}`, {});
  }

  getReadyOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/orders/ready`);
  }

  markServed(orderId: number): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/orders/${orderId}/mark-served`, {});
  }

  createWalkInOrder(orderData: any): Observable<Order> {
    return this.http.post<Order>(`${this.apiUrl}/orders/walk-in`, orderData);
  }

  getNotifications(): Observable<NotificationMsg[]> {
    return this.http.get<NotificationMsg[]>(`${this.apiUrl}/notifications`);
  }
}
