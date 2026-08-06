import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/cafe.models';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark shadow-sm py-3 px-4">
      <div class="container-fluid">
        <a class="navbar-brand d-flex align-items-center gap-2" routerLink="/">
          <i class="fa-solid fa-mug-hot text-warning fs-3"></i>
          <span class="font-serif fw-bold fs-4 text-white">Artisanal Cafe</span>
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarContent">
          <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarContent">
          <ul class="navbar-nav me-auto mb-2 mb-lg-0 gap-2 ms-lg-4">
            <!-- Customer Menu link -->
            <li class="nav-item">
              <a class="nav-link text-white-50" routerLink="/customer/menu" routerLinkActive="text-white fw-bold">
                <i class="fa-solid fa-utensils me-1"></i> Digital Menu
              </a>
            </li>

            <!-- Staff Links -->
            <ng-container *ngIf="user">
              <li class="nav-item" *ngIf="user.role === 'ROLE_KITCHEN' || user.role === 'ROLE_ADMIN'">
                <a class="nav-link text-white-50" routerLink="/kitchen/dashboard" routerLinkActive="text-white fw-bold">
                  <i class="fa-solid fa-fire-burner me-1"></i> Kitchen Board
                </a>
              </li>
              <li class="nav-item" *ngIf="user.role === 'ROLE_WAITER' || user.role === 'ROLE_ADMIN'">
                <a class="nav-link text-white-50" routerLink="/waiter/dashboard" routerLinkActive="text-white fw-bold">
                  <i class="fa-solid fa-chair me-1"></i> Active Tables
                </a>
              </li>
              <li class="nav-item" *ngIf="user.role === 'ROLE_CASHIER' || user.role === 'ROLE_ADMIN'">
                <a class="nav-link text-white-50" routerLink="/cashier/billing" routerLinkActive="text-white fw-bold">
                  <i class="fa-solid fa-file-invoice-dollar me-1"></i> Cashier Billing
                </a>
              </li>
              <li class="nav-item" *ngIf="user.role === 'ROLE_ADMIN'">
                <a class="nav-link text-white-50" routerLink="/admin/dashboard" routerLinkActive="text-white fw-bold">
                  <i class="fa-solid fa-chart-line me-1"></i> Admin Portal
                </a>
              </li>
            </ng-container>
          </ul>

          <div class="d-flex align-items-center gap-3">
            <ng-container *ngIf="user; else loginBtn">
              <div class="text-end text-white">
                <div class="fw-bold fs-6">{{ user.fullName }}</div>
                <span class="badge bg-warning text-dark me-2">{{ getRoleBadgeLabel(user.role) }}</span>
              </div>
              <button class="btn btn-outline-light rounded-pill btn-sm px-3" (click)="logout()">
                <i class="fa-solid fa-right-from-bracket me-1"></i> Logout
              </button>
            </ng-container>
            <ng-template #loginBtn>
              <a class="btn btn-warning text-dark fw-bold rounded-pill px-4" routerLink="/login">
                <i class="fa-solid fa-lock me-1"></i> Staff Login
              </a>
            </ng-template>
          </div>
        </div>
      </div>
    </nav>
  `
})
export class NavbarComponent {
  user: User | null = null;

  constructor(private authService: AuthService, private router: Router) {
    this.authService.currentUser$.subscribe(u => this.user = u);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  getRoleBadgeLabel(role: string): string {
    switch (role) {
      case 'ROLE_ADMIN': return 'ADMIN';
      case 'ROLE_KITCHEN': return 'CHEF';
      case 'ROLE_WAITER': return 'WAITER';
      case 'ROLE_CASHIER': return 'CASHIER';
      default: return 'STAFF';
    }
  }
}
