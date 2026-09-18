import { Injectable } from '@angular/core';
import { Client } from '@stomp/stompjs';
import * as SockJS_ from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { NotificationMsg } from '../models/cafe.models';
import { getBaseUrl } from './api.config';

const SockJS = (SockJS_ as any).default || SockJS_;

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private notificationSubject = new BehaviorSubject<NotificationMsg | null>(null);
  public notification$: Observable<NotificationMsg | null> = this.notificationSubject.asObservable();

  constructor() {
    this.initWebSocket();
  }

  private initWebSocket(): void {
    try {
      this.stompClient = new Client({
        webSocketFactory: () => new SockJS(`${getBaseUrl()}/ws`),
        reconnectDelay: 5000,
        debug: (str) => console.log('[STOMP]', str)
      });

      this.stompClient.onConnect = () => {
        console.log('Connected to WebSocket STOMP server');
        this.stompClient?.subscribe('/topic/notifications/all', (message) => {
          if (message.body) {
            const notif: NotificationMsg = JSON.parse(message.body);
            this.notificationSubject.next(notif);
            this.playNotificationSound();
          }
        });
      };

      this.stompClient.activate();
    } catch (e) {
      console.warn('WebSocket connection error, using polling fallback.', e);
    }
  }

  public playNotificationSound(): void {
    try {
      const audioCtx = new (window.AudioContext || (window as any).webkitAudioContext)();
      const osc = audioCtx.createOscillator();
      const gain = audioCtx.createGain();

      osc.type = 'sine';
      osc.frequency.setValueAtTime(587.33, audioCtx.currentTime); // D5
      osc.frequency.exponentialRampToValueAtTime(880, audioCtx.currentTime + 0.15); // A5

      gain.gain.setValueAtTime(0.3, audioCtx.currentTime);
      gain.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.3);

      osc.connect(gain);
      gain.connect(audioCtx.destination);

      osc.start();
      osc.stop(audioCtx.currentTime + 0.3);
    } catch (e) {
      console.log('Web Audio notification sound disabled or blocked by browser.');
    }
  }
}
