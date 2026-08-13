import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { User } from '../models/cafe.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    const savedUser = localStorage.getItem('cafe_user');
    if (savedUser) {
      try {
        this.currentUserSubject.next(JSON.parse(savedUser));
      } catch (e) {
        localStorage.removeItem('cafe_user');
      }
    }
  }

  login(credentials: { username: string; password: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, credentials).pipe(
      tap(res => {
        const user: User = {
          id: res.id,
          username: res.username,
          email: res.email,
          fullName: res.fullName,
          role: res.role,
          token: res.token
        };
        localStorage.setItem('cafe_user', JSON.stringify(user));
        this.currentUserSubject.next(user);
      })
    );
  }

  register(userData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, userData);
  }

  logout(): void {
    localStorage.removeItem('cafe_user');
    this.currentUserSubject.next(null);
  }

  getUserToken(): string | null {
    return this.currentUserSubject.value?.token || null;
  }

  getUserRole(): string | null {
    return this.currentUserSubject.value?.role || null;
  }

  isLoggedIn(): boolean {
    return !!this.getUserToken();
  }
}
