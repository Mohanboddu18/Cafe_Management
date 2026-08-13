import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RestaurantTable, Invoice } from '../models/cafe.models';

@Injectable({
  providedIn: 'root'
})
export class CashierService {
  private apiUrl = 'http://localhost:8080/api/cashier';

  constructor(private http: HttpClient) {}

  getActiveTables(): Observable<RestaurantTable[]> {
    return this.http.get<RestaurantTable[]>(`${this.apiUrl}/tables/active`);
  }

  applyCoupon(couponCode: string, subtotal: number): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/coupon/apply`, { couponCode, subtotal });
  }

  processPayment(paymentData: any): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/payment/process`, paymentData);
  }

  getInvoiceByOrderId(orderId: number): Observable<Invoice> {
    return this.http.get<Invoice>(`${this.apiUrl}/invoice/order/${orderId}`);
  }

  downloadInvoicePdf(orderId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/invoice/order/${orderId}/pdf`, { responseType: 'blob' }) as Observable<Blob>;
  }

  generateBillForCustomer(orderId: number, couponCode?: string, gstPercentage: number = 5.0): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/invoice/generate`, { orderId, couponCode, gstPercentage });
  }

  confirmCashPayment(orderId: number): Observable<Invoice> {
    return this.http.post<Invoice>(`${this.apiUrl}/invoice/confirm-cash/${orderId}`, {});
  }
}
