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
import { PaymentQuoteRequest, PaymentQuoteResponse, PaymentTaxDetail } from '../../../../core/models/transaction.model';
import { TransactionApiService } from '../../../../core/services/transaction-api.service';
import { createIdempotencyKey } from '../../../../core/utils/idempotency-key.util';
import { EMPTY, Subject, catchError, switchMap, timer } from 'rxjs';
import {
  MAX_PAYMENT_AMOUNT,
  MIN_TRANSACTION_AMOUNT,
} from '../../../../core/constants/transaction-limits';

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
  private readonly paymentQuoteRequests = new Subject<PaymentQuoteRequest | null>();
  private errorToastTimer: ReturnType<typeof setTimeout> | null = null;
  readonly errorToastDurationMs = 4500;
  readonly minPaymentAmount = MIN_TRANSACTION_AMOUNT;
  readonly maxPaymentAmount = MAX_PAYMENT_AMOUNT;

  accountBalance = 0;
  isSubmitting = false;
  showConfirmationModal = false;
  errorMessage: string | null = null;
  currentUser: UserSummary | null = null;
  paymentQuote: PaymentQuoteResponse | null = null;
  isLoadingPaymentQuote = false;
  paymentQuoteErrorMessage: string | null = null;

  selectedWallet: WalletPaymentTarget | null = null;
  selectedAmount: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletStore: WalletStoreService,
    private authService: AuthService,
    private merchantApi: MerchantApiService,
    private merchantMapper: MerchantMapperService,
    private transactionApi: TransactionApiService
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getCurrentUser();
    this.setupPaymentQuotePreview();

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
    const walletId = this.route.snapshot.paramMap.get('merchantId') || '';

    if (state && state.wallet && state.wallet.id === walletId) {
      this.selectedWallet = { ...state.wallet, merchantName: state.wallet.name };
      this.requestPaymentQuote();
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
        this.requestPaymentQuote();
      },
      error: () => {
        this.router.navigate(['/topup']);
      },
    });
  }

  get adminFee(): number {
    return this.currentPaymentQuote?.taxAmount ?? 0;
  }

  get taxDetails(): PaymentTaxDetail[] {
    return this.currentPaymentQuote?.taxDetails ?? [];
  }

  get hasCurrentPaymentQuote(): boolean {
    return this.currentPaymentQuote !== null;
  }

  get recipientName(): string {
    return this.currentUser?.name || 'Akun WalletPay';
  }

  get recipientContact(): string {
    return this.currentUser?.email || 'Email akun tidak tersedia';
  }

  get totalPaymentAmount(): number {
    return this.currentPaymentQuote?.amount ?? this.selectedAmount;
  }

  get balanceAfterPayment(): number {
    return this.accountBalance - this.totalPaymentAmount;
  }

  get isPaymentAmountValid(): boolean {
    return this.selectedAmount >= this.minPaymentAmount
      && this.hasCurrentPaymentQuote
      && this.totalPaymentAmount <= this.maxPaymentAmount;
  }

  onAmountChange(amount: number): void {
    this.selectedAmount = amount;
    this.dismissErrorToast();
    this.requestPaymentQuote();
  }

  onPay(): void {
    if (this.isSubmitting || !this.selectedWallet) {
      return;
    }

    if (!this.isPaymentAmountValid) {
      this.showErrorToast(`Nominal payment harus Rp ${this.format(this.minPaymentAmount)} - Rp ${this.format(this.maxPaymentAmount)}.`);
      return;
    }

    if (this.accountBalance < this.totalPaymentAmount) {
      this.showErrorToast('Pembayaran gagal diproses. Saldo tidak cukup.');
      return;
    }

    this.showConfirmationModal = true;
  }

  closeConfirmationModal(): void {
    this.showConfirmationModal = false;
  }

  confirmPayment(): void {
    if (this.isSubmitting || !this.selectedWallet || !this.isPaymentAmountValid) {
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
        this.showConfirmationModal = false;

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
        this.showConfirmationModal = false;
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

  taxValueLabel(tax: PaymentTaxDetail): string {
    if (tax.valueType === 'PERCENTAGE') {
      return `${tax.taxValue}%`;
    }

    return `Rp ${this.format(tax.taxValue)}`;
  }

  private get currentPaymentQuote(): PaymentQuoteResponse | null {
    if (!this.paymentQuote || !this.selectedWallet) {
      return null;
    }

    if (this.paymentQuote.merchantName !== this.selectedWallet.merchantName) {
      return null;
    }

    return this.paymentQuote.baseAmount === this.selectedAmount ? this.paymentQuote : null;
  }

  private setupPaymentQuotePreview(): void {
    this.paymentQuoteRequests
      .pipe(
        switchMap((request) => {
          if (!request) {
            return EMPTY;
          }

          this.isLoadingPaymentQuote = true;
          this.paymentQuoteErrorMessage = null;

          return timer(250).pipe(
            switchMap(() => this.transactionApi.quotePayment(request)),
            catchError(() => {
              this.paymentQuote = null;
              this.isLoadingPaymentQuote = false;
              this.paymentQuoteErrorMessage = 'Gagal menghitung rincian pajak terbaru.';
              return EMPTY;
            })
          );
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((response) => {
        this.paymentQuote = response.data;
        this.isLoadingPaymentQuote = false;
        this.paymentQuoteErrorMessage = null;
      });
  }

  private requestPaymentQuote(): void {
    this.paymentQuote = null;
    this.paymentQuoteErrorMessage = null;

    if (!this.selectedWallet || !this.isAmountEligibleForQuote) {
      this.isLoadingPaymentQuote = false;
      this.paymentQuoteRequests.next(null);
      return;
    }

    this.paymentQuoteRequests.next({
      merchantName: this.selectedWallet.merchantName,
      amount: this.selectedAmount,
    });
  }

  private get isAmountEligibleForQuote(): boolean {
    return this.selectedAmount >= this.minPaymentAmount
      && this.selectedAmount <= this.maxPaymentAmount;
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
