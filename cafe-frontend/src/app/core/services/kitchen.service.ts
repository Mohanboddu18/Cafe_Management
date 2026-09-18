import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order, NotificationMsg } from '../models/cafe.models';
import { getBaseUrl } from './api.config';

@Injectable({
  providedIn: 'root'
})
export class KitchenService {
  private apiUrl = `${getBaseUrl()}/api/kitchen`;

  constructor(private http: HttpClient) {}

  getLiveOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.apiUrl}/orders/live`);
  }

  updateOrderStatus(orderId: number, status: string, estimatedPrepTime?: number): Observable<Order> {
    return this.http.put<Order>(`${this.apiUrl}/orders/${orderId}/status`, { status, estimatedPrepTime });
  }

  getNotifications(): Observable<NotificationMsg[]> {
    return this.http.get<NotificationMsg[]>(`${this.apiUrl}/notifications`);
  }
}
