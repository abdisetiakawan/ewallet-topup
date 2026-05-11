import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AmountSelectorComponent } from '../../components/amount-selector/amount-selector.component';
import { PaymentSummaryComponent } from '../../components/payment-summary/payment-summary.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MerchantApiService } from '../../../../core/services/merchant-api.service';
import { UserSummary } from '../../../../core/models/auth.model';

type WalletPaymentTarget = EWallet & {
  merchantName: string;
};

@Component({
  selector: 'app-detail-pembayaran',
  standalone: true,
  imports: [
    CommonModule,
    AmountSelectorComponent,
    PaymentSummaryComponent,
  ],
  templateUrl: './detail-pembayaran.component.html',
  styleUrl: './detail-pembayaran.component.css',
})
export class DetailPembayaranComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  accountBalance = 0;
  isSubmitting = false;
  errorMessage: string | null = null;
  currentUser: UserSummary | null = null;

  selectedWallet: WalletPaymentTarget | null = null;
  selectedAmount: number = 0;

  private readonly uiMetadata: Record<string, Partial<EWallet>> = {
    'gopay': { id: 'gopay', icon: 'payments', iconBgColor: '#e5eeff', iconTextColor: '#0058be' },
    'ovo': { id: 'ovo', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA' },
    'dana': { id: 'dana', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be' },
    'shopeepay': { id: 'shopeepay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D' },
    'linkaja': { id: 'linkaja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a' },
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletStore: WalletStoreService,
    private authService: AuthService,
    private merchantApi: MerchantApiService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();

    this.walletStore.balance$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((balance) => {
        this.accountBalance = balance;
      });

    this.walletStore.loadBalance().subscribe({
      error: () => {
        this.errorMessage = 'Gagal memuat saldo terbaru.';
      },
    });

    const state = typeof window !== 'undefined' ? window.history.state : null;
    const walletId = this.route.snapshot.paramMap.get('walletId') || '';

    if (state && state.wallet && state.wallet.id === walletId) {
      this.selectedWallet = { ...state.wallet, merchantName: state.wallet.name };
    } else {
      this.fetchMerchant(walletId);
    }
  }

  fetchMerchant(walletId: string): void {
    this.merchantApi.getAllMerchants().subscribe({
      next: (res) => {
        const merchant = res.data.find(m => m.name.toLowerCase() === walletId.toLowerCase());
        if (!merchant) {
           this.router.navigate(['/topup']);
           return;
        }

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

        this.selectedWallet = {
            ...meta,
            id: meta.id as string,
            name: merchant.name,
            icon: meta.icon as string,
            iconBgColor: meta.iconBgColor as string,
            iconTextColor: meta.iconTextColor as string,
            feeValue,
            feeType,
            feeLabel,
            merchantName: merchant.name
        };
      },
      error: () => {
        this.router.navigate(['/topup']);
      }
    });
  }

  get adminFee(): number {
    if (!this.selectedWallet) return 0;
    if (this.selectedWallet.feeType === 'PERCENTAGE') {
      return Math.round((this.selectedAmount * this.selectedWallet.feeValue) / 100);
    }
    return this.selectedWallet.feeValue;
  }

  get recipientName(): string {
    return this.currentUser?.name || 'Akun WalletPay';
  }

  get recipientContact(): string {
    return this.currentUser?.email || 'Email akun tidak tersedia';
  }

  onAmountChange(amount: number): void {
    this.selectedAmount = amount;
  }

  onPay(): void {
    if (this.isSubmitting || this.selectedAmount < 10000 || !this.selectedWallet) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    // Send the base amount; backend will calculate and add the tax.
    this.walletStore.pay({
      merchantName: this.selectedWallet.merchantName,
      amount: this.selectedAmount,
      description: `Top-up ${this.selectedWallet.name} untuk ${this.recipientName}`,
    }).subscribe({
      next: (response) => {
        const finalAmount = response.data.amount; // backend returns total amount deducted
        this.router.navigate(['/topup'], {
          state: {
            successNotification: {
              icon: 'check_circle',
              title: 'Payment sukses',
              message: '',
              amountLabel: `- Rp ${this.format(finalAmount)}`,
            },
          },
        });
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'Pembayaran gagal diproses. Pastikan saldo cukup dan coba lagi.';
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/topup']);
  }

  format(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }
}
