import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID, inject } from '@angular/core';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { BalanceCardComponent } from '../../components/balance-card/balance-card.component';
import { EwalletCardComponent } from '../../components/ewallet-card/ewallet-card.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { MerchantApiService } from '../../../../core/services/merchant-api.service';
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

  ewallets: EWallet[] = [];
  
  private readonly uiMetadata: Record<string, Partial<EWallet>> = {
    'gopay': { id: 'gopay', icon: 'payments', iconBgColor: '#e5eeff', iconTextColor: '#0058be', featured: true },
    'ovo': { id: 'ovo', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA' },
    'dana': { id: 'dana', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be' },
    'shopeepay': { id: 'shopeepay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D' },
    'linkaja': { id: 'linkaja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a' },
  };

  constructor(
    private router: Router,
    private walletStore: WalletStoreService,
    private merchantApi: MerchantApiService
  ) {
    this.balance$ = this.walletStore.balance$;
  }

  ngOnInit(): void {
    this.successNotification = this.resolveSuccessNotification();
    this.scheduleSuccessToastDismiss();
    this.walletStore.loadBalance().subscribe();
    this.fetchMerchants();
  }

  ngOnDestroy(): void {
    this.clearSuccessToastTimer();
  }

  fetchMerchants(): void {
    this.merchantApi.getAllMerchants().subscribe({
      next: (res) => {
        const merchants = res.data;
        this.ewallets = merchants.map(merchant => {
          const lowerName = merchant.name.toLowerCase();
          const meta = this.uiMetadata[lowerName] || {
             id: lowerName,
             icon: 'account_balance_wallet',
             iconBgColor: '#f3f4f6',
             iconTextColor: '#374151'
          };
          
          let totalFixed = 0;
          let totalPercentage = 0;
          
          merchant.taxes.forEach(tax => {
            if (tax.valueType === 'FIXED') totalFixed += tax.taxValue;
            if (tax.valueType === 'PERCENTAGE') totalPercentage += tax.taxValue;
          });
          
          let feeLabel = 'Bebas biaya';
          let feeValue = 0;
          let feeType: 'FIXED' | 'PERCENTAGE' = 'FIXED';
          
          if (totalPercentage > 0) {
             feeLabel = `Biaya ${totalPercentage}%`;
             feeValue = totalPercentage;
             feeType = 'PERCENTAGE';
          } else if (totalFixed > 0) {
             feeLabel = `Biaya Rp ${new Intl.NumberFormat('id-ID').format(totalFixed)}`;
             feeValue = totalFixed;
             feeType = 'FIXED';
          }
          
          return {
            ...meta,
            id: meta.id as string,
            name: merchant.name,
            icon: meta.icon as string,
            iconBgColor: meta.iconBgColor as string,
            iconTextColor: meta.iconTextColor as string,
            feeValue,
            feeType,
            feeLabel,
            featured: meta.featured
          };
        });
      },
      error: (err) => console.error('Failed to fetch merchants', err)
    });
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
