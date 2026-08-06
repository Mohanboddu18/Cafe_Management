import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './shared/navbar/navbar.component';
import { ToastComponent } from './shared/toast/toast.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, NavbarComponent, ToastComponent],
  template: `
    <div class="min-vh-100 d-flex flex-column">
      <app-navbar></app-navbar>
      <main class="flex-grow-1">
        <router-outlet></router-outlet>
      </main>
      <app-toast></app-toast>
      <footer class="bg-dark text-white-50 py-3 text-center small mt-auto border-top border-secondary">
        <div class="container">
          <span class="font-serif text-warning fw-bold">Artisanal Cafe & Bistro</span> &copy; 2026. All Rights Reserved. Built with Angular 20 & Spring Boot.
        </div>
      </footer>
    </div>
  `
})
export class AppComponent {
  title = 'cafe-frontend';
}
