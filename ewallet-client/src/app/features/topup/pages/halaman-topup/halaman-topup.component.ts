import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { createIdempotencyKey } from '../../../../core/utils/idempotency-key.util';
import {
  MAX_TOPUP_AMOUNT,
  MIN_TRANSACTION_AMOUNT,
  parseTransactionAmount,
} from '../../../../core/constants/transaction-limits';

interface TopupAmountOption {
  id: string;
  label: string;
  amount: number;
  badge: string;
}

@Component({
  selector: 'app-halaman-topup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './halaman-topup.component.html',
  styleUrl: './halaman-topup.component.css',
})
export class HalamanTopupComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly amountFormatter = new Intl.NumberFormat('id-ID');

  balance = 0;
  selectedAmount = 100000;
  customAmount = '100.000';
  showConfirmationModal = false;
  isSubmitting = false;
  errorMessage: string | null = null;
  isManualAmountOverLimit = false;
  readonly minTopupAmount = MIN_TRANSACTION_AMOUNT;
  readonly maxTopupAmount = MAX_TOPUP_AMOUNT;

  readonly amountOptions: TopupAmountOption[] = [
    { id: '50k', label: 'Rp 50rb', amount: 50000, badge: 'Hemat' },
    { id: '100k', label: 'Rp 100rb', amount: 100000, badge: 'Populer' },
    { id: '200k', label: 'Rp 200rb', amount: 200000, badge: 'Standar' },
    { id: '500k', label: 'Rp 500rb', amount: 500000, badge: 'Maksimal' },
  ];

  constructor(
    private router: Router,
    private walletStore: WalletStoreService
  ) {}

  ngOnInit(): void {
    this.walletStore.balance$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((balance) => {
        this.balance = balance;
      });

    this.walletStore.loadBalance().subscribe({
      error: () => {
        this.errorMessage = 'Gagal memuat saldo terbaru.';
      },
    });
  }

  get formattedBalance(): string {
    return this.formatAmount(this.balance);
  }

  get formattedSelectedAmount(): string {
    return this.formatAmount(this.selectedAmount);
  }

  get formattedBalanceAfterTopup(): string {
    return this.formatAmount(this.balance + this.selectedAmount);
  }

  get isValidAmount(): boolean {
    return !this.isManualAmountOverLimit
      && this.selectedAmount >= this.minTopupAmount
      && this.selectedAmount <= this.maxTopupAmount;
  }

  get amountErrorMessage(): string {
    if (this.selectedAmount > this.maxTopupAmount) {
      return `Maksimal top-up Rp ${this.formatAmount(this.maxTopupAmount)}`;
    }

    return `Minimal top-up Rp ${this.formatAmount(this.minTopupAmount)}`;
  }

  isSelectedAmount(amount: number): boolean {
    return this.selectedAmount === amount;
  }

  selectAmount(amount: number): void {
    this.selectedAmount = amount;
    this.customAmount = this.formatAmount(amount);
    this.isManualAmountOverLimit = false;
  }

  onCustomAmountChange(value: string): void {
    const parsedAmount = parseTransactionAmount(value, this.maxTopupAmount);

    this.selectedAmount = parsedAmount.amount;
    this.customAmount = parsedAmount.displayValue;
    this.isManualAmountOverLimit = parsedAmount.exceedsLimit;
  }

  formatCustomAmount(): void {
    if (this.selectedAmount > 0) {
      this.customAmount = this.formatAmount(this.selectedAmount);
    }
  }

  continuePayment(): void {
    if (!this.isValidAmount) {
      return;
    }

    this.showConfirmationModal = true;
  }

  closeConfirmationModal(): void {
    this.showConfirmationModal = false;
  }

  confirmTopup(): void {
    if (!this.isValidAmount || this.isSubmitting) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;
    const submittedAmount = this.selectedAmount;
    const idempotencyKey = createIdempotencyKey();

    this.walletStore.topup(submittedAmount, idempotencyKey).subscribe({
      next: () => {
        this.showConfirmationModal = false;
        this.router.navigate(['/topup'], {
          state: {
            successNotification: {
              icon: 'check_circle',
              title: 'Topup sukses',
              message: '',
              amountLabel: `+ Rp ${this.formatAmount(submittedAmount)}`,
            },
          },
        });
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'Top-up gagal diproses. Silakan coba lagi.';
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/topup']);
  }

  private formatAmount(amount: number): string {
    return this.amountFormatter.format(amount);
  }
}
