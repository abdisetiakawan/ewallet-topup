import { Component, DestroyRef, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AmountSelectorComponent } from '../../components/amount-selector/amount-selector.component';
import { PaymentSummaryComponent } from '../../components/payment-summary/payment-summary.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { AuthService } from '../../../../core/services/auth.service';
import { MerchantApiService } from '../../../../core/services/merchant-api.service';
import { MerchantMapperService } from '../../../../core/services/merchant-mapper.service';
import { UserSummary } from '../../../../core/models/auth.model';
import { createIdempotencyKey } from '../../../../core/utils/idempotency-key.util';

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
export class DetailPembayaranComponent implements OnInit, OnDestroy {
  private readonly destroyRef = inject(DestroyRef);
  private errorToastTimer: ReturnType<typeof setTimeout> | null = null;
  readonly errorToastDurationMs = 4500;

  accountBalance = 0;
  isSubmitting = false;
  errorMessage: string | null = null;
  currentUser: UserSummary | null = null;

  selectedWallet: WalletPaymentTarget | null = null;
  selectedAmount: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletStore: WalletStoreService,
    private authService: AuthService,
    private merchantApi: MerchantApiService,
    private merchantMapper: MerchantMapperService
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
        this.showErrorToast('Gagal memuat saldo terbaru.');
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

  ngOnDestroy(): void {
    this.clearErrorToastTimer();
  }

  fetchMerchant(walletId: string): void {
    this.merchantApi.getAllMerchants().subscribe({
      next: (res) => {
        const merchant = res.data.find(
          (m) => m.name.toLowerCase() === walletId.toLowerCase()
        );

        if (!merchant) {
          this.router.navigate(['/topup']);
          return;
        }

        this.selectedWallet = {
          ...this.merchantMapper.mapToEWallet(merchant),
          merchantName: merchant.name,
        };
      },
      error: () => {
        this.router.navigate(['/topup']);
      },
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
    this.dismissErrorToast();
  }

  onPay(): void {
    if (this.isSubmitting || this.selectedAmount < 10000 || !this.selectedWallet) {
      return;
    }

    this.isSubmitting = true;
    this.dismissErrorToast();

    const idempotencyKey = createIdempotencyKey();

    this.walletStore.pay({
      merchantName: this.selectedWallet.merchantName,
      amount: this.selectedAmount,
      description: `Top-up ${this.selectedWallet.name} untuk ${this.recipientName}`,
    }, idempotencyKey).subscribe({
      next: (response) => {
        const finalAmount = response.data.amount;

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
        this.showErrorToast('Pembayaran gagal diproses. Pastikan saldo cukup dan coba lagi.');
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/topup']);
  }

  dismissErrorToast(): void {
    this.clearErrorToastTimer();
    this.errorMessage = null;
  }

  format(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }

  private showErrorToast(message: string): void {
    this.errorMessage = message;
    this.clearErrorToastTimer();
    this.errorToastTimer = setTimeout(() => {
      this.errorMessage = null;
      this.errorToastTimer = null;
    }, this.errorToastDurationMs);
  }

  private clearErrorToastTimer(): void {
    if (this.errorToastTimer) {
      clearTimeout(this.errorToastTimer);
      this.errorToastTimer = null;
    }
  }
}
