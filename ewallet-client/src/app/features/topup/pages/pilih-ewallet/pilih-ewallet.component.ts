import { Component, DestroyRef, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { BalanceCardComponent } from '../../components/balance-card/balance-card.component';
import { EwalletCardComponent } from '../../components/ewallet-card/ewallet-card.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { MerchantApiService } from '../../../../core/services/merchant-api.service';
import { MerchantMapperService } from '../../../../core/services/merchant-mapper.service';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';
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
    TopAppBarComponent,
    BottomNavBarComponent,
    BalanceCardComponent,
    EwalletCardComponent,
  ],
  templateUrl: './pilih-ewallet.component.html',
  styleUrl: './pilih-ewallet.component.css',
})
export class PilihEwalletComponent implements OnInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly destroyRef = inject(DestroyRef);
  private successToastTimer: ReturnType<typeof setTimeout> | null = null;
  readonly successToastDurationMs = 4500;

  readonly balance$: Observable<number>;
  successNotification: SuccessNotification | null = null;

  ewallets: EWallet[] = [];
  merchantsError: string | null = null;
  isLoadingMerchants = false;

  constructor(
    private router: Router,
    private walletStore: WalletStoreService,
    private merchantApi: MerchantApiService,
    private merchantMapper: MerchantMapperService
  ) {
    this.balance$ = this.walletStore.balance$;
  }

  ngOnInit(): void {
    this.successNotification = this.resolveSuccessNotification();
    this.scheduleSuccessToastDismiss();
    this.walletStore.loadBalance()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe();
    this.fetchMerchants();
  }

  ngOnDestroy(): void {
    this.clearSuccessToastTimer();
  }

  fetchMerchants(): void {
    this.isLoadingMerchants = true;
    this.merchantsError = null;

    this.merchantApi.getAllMerchants()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (res) => {
          this.ewallets = this.merchantMapper.mapAllToEWallets(res.data);
          this.isLoadingMerchants = false;
        },
        error: () => {
          this.isLoadingMerchants = false;
          this.merchantsError = 'Gagal memuat daftar e-wallet. Periksa koneksi dan coba lagi.';
        },
      });
  }

  retryFetchMerchants(): void {
    this.fetchMerchants();
  }

  onWalletSelected(wallet: EWallet): void {
    this.router.navigate(['/topup/detail', wallet.id], { state: { wallet } });
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
