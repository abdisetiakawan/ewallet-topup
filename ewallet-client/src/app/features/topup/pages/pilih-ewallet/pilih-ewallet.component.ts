import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID, inject } from '@angular/core';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { BalanceCardComponent } from '../../components/balance-card/balance-card.component';
import { EwalletCardComponent } from '../../components/ewallet-card/ewallet-card.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { Observable } from 'rxjs';

interface SuccessNotification {
  icon: string;
  title: string;
  message: string;
  amountLabel: string;
}

@Component({
  selector: 'app-pilih-ewallet',
  standalone: true,
  imports: [
    CommonModule,
    BottomNavBarComponent,
    BalanceCardComponent,
    EwalletCardComponent,
  ],
  templateUrl: './pilih-ewallet.component.html',
  styleUrl: './pilih-ewallet.component.css',
})
export class PilihEwalletComponent implements OnInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private successToastTimer: ReturnType<typeof setTimeout> | null = null;
  readonly successToastDurationMs = 4500;

  readonly balance$: Observable<number>;
  successNotification: SuccessNotification | null = null;

  readonly ewallets: EWallet[] = [
    { id: 'gopay', name: 'GoPay', icon: 'payments', iconBgColor: '#e5eeff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Bebas biaya admin', featured: true },
    { id: 'ovo', name: 'OVO', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA', adminFee: 0, feeLabel: 'Instan' },
    { id: 'dana', name: 'DANA', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Instan' },
    { id: 'shopeepay', name: 'ShopeePay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D', adminFee: 500, feeLabel: 'Biaya Rp 500' },
    { id: 'linkaja', name: 'LinkAja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a', adminFee: 0, feeLabel: 'Instan' },
  ];

  constructor(
    private router: Router,
    private walletStore: WalletStoreService
  ) {
    this.balance$ = this.walletStore.balance$;
  }

  ngOnInit(): void {
    this.successNotification = this.resolveSuccessNotification();
    this.scheduleSuccessToastDismiss();
    this.walletStore.loadBalance().subscribe();
  }

  ngOnDestroy(): void {
    this.clearSuccessToastTimer();
  }

  onWalletSelected(wallet: EWallet): void {
    this.router.navigate(['/topup/detail', wallet.id]);
  }

  closeSuccessNotification(): void {
    this.clearSuccessToastTimer();
    this.successNotification = null;
  }

  private scheduleSuccessToastDismiss(): void {
    if (!this.successNotification || !isPlatformBrowser(this.platformId)) {
      return;
    }

    this.clearSuccessToastTimer();
    this.successToastTimer = setTimeout(() => {
      this.successNotification = null;
      this.successToastTimer = null;
    }, this.successToastDurationMs);
  }

  private clearSuccessToastTimer(): void {
    if (this.successToastTimer) {
      clearTimeout(this.successToastTimer);
      this.successToastTimer = null;
    }
  }

  private resolveSuccessNotification(): SuccessNotification | null {
    const state = this.router.getCurrentNavigation()?.extras.state ?? this.getBrowserHistoryState();
    const notification = state?.['successNotification'];

    if (!this.isSuccessNotification(notification)) {
      return null;
    }

    this.clearBrowserHistoryState();
    return notification;
  }

  private getBrowserHistoryState(): Record<string, unknown> | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    return window.history.state as Record<string, unknown>;
  }

  private clearBrowserHistoryState(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    window.history.replaceState({}, document.title, window.location.href);
  }

  private isSuccessNotification(value: unknown): value is SuccessNotification {
    if (typeof value !== 'object' || value === null) {
      return false;
    }

    const notification = value as Record<string, unknown>;
    return (
      typeof notification['icon'] === 'string' &&
      typeof notification['title'] === 'string' &&
      typeof notification['message'] === 'string' &&
      typeof notification['amountLabel'] === 'string'
    );
  }
}
