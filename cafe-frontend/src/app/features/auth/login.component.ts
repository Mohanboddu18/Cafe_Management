import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-md-6 col-lg-5">
          <div class="glass-card p-4 p-md-5 text-center position-relative">
            <div class="mb-4">
              <div class="icon-circle bg-warning text-dark mx-auto mb-3 d-flex align-items-center justify-content-center rounded-circle" style="width: 70px; height: 70px;">
                <i class="fa-solid fa-user-shield fs-2"></i>
              </div>
              <h2 class="font-serif fw-bold text-dark mb-1">Staff Portal Login</h2>
              <p class="text-muted small">Access Kitchen, Waiter, Cashier or Admin Dashboards</p>
            </div>

            <form (ngSubmit)="onSubmit()">
              <div class="form-floating mb-3 text-start">
                <input type="text" class="form-control rounded-4" id="username" placeholder="Username" [(ngModel)]="username" name="username" required>
                <label for="username"><i class="fa-solid fa-user me-2 text-muted"></i>Username</label>
              </div>

              <div class="form-floating mb-4 text-start">
                <input type="password" class="form-control rounded-4" id="password" placeholder="Password" [(ngModel)]="password" name="password" required>
                <label for="password"><i class="fa-solid fa-key me-2 text-muted"></i>Password</label>
              </div>

              <button type="submit" class="btn btn-cafe w-100 py-3 rounded-pill fw-bold text-uppercase tracking-wider" [disabled]="loading">
                <span *ngIf="loading" class="spinner-border spinner-border-sm me-2"></span>
                Sign In
              </button>
            </form>

            <div class="mt-4 pt-3 border-top text-start">
              <span class="badge bg-secondary mb-2 d-block text-center">Quick Demo Credentials</span>
              <div class="d-flex flex-wrap gap-2 justify-content-center small">
                <button class="btn btn-sm btn-outline-dark rounded-pill" (click)="fillDemo('admin', 'admin123')">Admin</button>
                <button class="btn btn-sm btn-outline-dark rounded-pill" (click)="fillDemo('kitchen', 'kitchen123')">Chef</button>
                <button class="btn btn-sm btn-outline-dark rounded-pill" (click)="fillDemo('waiter', 'waiter123')">Waiter</button>
                <button class="btn btn-sm btn-outline-dark rounded-pill" (click)="fillDemo('cashier', 'cashier123')">Cashier</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class LoginComponent {
  username = '';
  password = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  fillDemo(u: string, p: string): void {
    this.username = u;
    this.password = p;
  }

  onSubmit(): void {
    if (!this.username || !this.password) {
      this.toastService.show('Please fill in username and password', 'warning');
      return;
    }

    this.loading = true;
    this.authService.login({ username: this.username, password: this.password }).subscribe({
      next: (res) => {
        this.loading = false;
        this.toastService.show(`Welcome back, ${res.fullName}!`, 'success');
        
        switch (res.role) {
          case 'ROLE_ADMIN': this.router.navigate(['/admin/dashboard']); break;
          case 'ROLE_KITCHEN': this.router.navigate(['/kitchen/dashboard']); break;
          case 'ROLE_WAITER': this.router.navigate(['/waiter/dashboard']); break;
          case 'ROLE_CASHIER': this.router.navigate(['/cashier/billing']); break;
          default: this.router.navigate(['/customer/menu']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.toastService.show(err.error?.message || 'Login failed. Please check credentials.', 'error');
      }
    });
  }
}
