import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService, ToastMessage } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 1100;">
      <div *ngFor="let toast of toasts" 
           class="toast show align-items-center text-white border-0 mb-2 shadow-lg"
           [ngClass]="{
             'bg-success': toast.type === 'success',
             'bg-danger': toast.type === 'error',
             'bg-warning text-dark': toast.type === 'warning',
             'bg-info text-dark': toast.type === 'info'
           }"
           role="alert">
        <div class="d-flex">
          <div class="toast-body fw-medium">
            <i [ngClass]="{
              'fa-solid fa-circle-check': toast.type === 'success',
              'fa-solid fa-triangle-exclamation': toast.type === 'error',
              'fa-solid fa-bell': toast.type === 'warning',
              'fa-solid fa-circle-info': toast.type === 'info'
            }" class="me-2"></i>
            {{ toast.text }}
          </div>
          <button type="button" class="btn-close me-2 m-auto" (click)="close(toast.id!)"></button>
        </div>
      </div>
    </div>
  `
})
export class ToastComponent {
  toasts: ToastMessage[] = [];

  constructor(private toastService: ToastService) {
    this.toastService.toasts$.subscribe(t => this.toasts = t);
  }

  close(id: number): void {
    this.toastService.remove(id);
  }
}
