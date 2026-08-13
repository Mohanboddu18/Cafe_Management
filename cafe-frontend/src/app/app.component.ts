import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, Router, NavigationEnd } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { filter, Subscription } from 'rxjs';
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
export class AppComponent implements OnInit, OnDestroy {
  title = 'cafe-frontend';
  private routerSub?: Subscription;

  constructor(private router: Router, private titleService: Title) {}

  ngOnInit(): void {
    this.updateBrowserTabTitle();
    this.routerSub = this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateBrowserTabTitle();
    });
  }

  ngOnDestroy(): void {
    this.routerSub?.unsubscribe();
  }

  updateBrowserTabTitle(): void {
    const url = this.router.url;
    let tableNum: string | null = null;
    if (typeof window !== 'undefined') {
      const urlParams = new URLSearchParams(window.location.search);
      tableNum = urlParams.get('table') || sessionStorage.getItem('current_table_number') || localStorage.getItem('cafe_customer_table');
    }

    let tabTitle = 'Artisanal Cafe & Bistro';

    if (url.includes('/kitchen/dashboard')) {
      tabTitle = 'Kitchen Board | Artisanal Cafe';
    } else if (url.includes('/cashier/billing')) {
      tabTitle = 'Cashier Billing | Artisanal Cafe';
    } else if (url.includes('/waiter/dashboard')) {
      tabTitle = 'Active Tables | Artisanal Cafe';
    } else if (url.includes('/admin/dashboard')) {
      tabTitle = 'Admin Portal | Artisanal Cafe';
    } else if (url.includes('/customer/menu')) {
      tabTitle = tableNum ? `Table #${tableNum} Digital Menu | Artisanal Cafe` : 'Digital Menu | Artisanal Cafe';
    } else if (url.includes('/customer/tracking')) {
      tabTitle = tableNum ? `Table #${tableNum} Order Tracking | Artisanal Cafe` : 'Order Tracking & Bill | Artisanal Cafe';
    } else if (url.includes('/customer/tables')) {
      tabTitle = 'Select Table | Artisanal Cafe';
    } else if (url.includes('/login')) {
      tabTitle = 'Staff Login | Artisanal Cafe';
    }

    this.titleService.setTitle(tabTitle);
  }
}
