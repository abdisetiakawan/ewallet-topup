import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import {
  TransactionDetail,
  TransactionStatus,
  TransactionTaxDetail,
  TransactionType,
} from '../../../../core/models/transaction.model';
import { TransactionApiService } from '../../../../core/services/transaction-api.service';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';

@Component({
  selector: 'app-transaction-detail',
  standalone: true,
  imports: [CommonModule, BottomNavBarComponent],
  templateUrl: './transaction-detail.component.html',
})
export class TransactionDetailComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private transactionId: number | null = null;

  transaction: TransactionDetail | null = null;
  isLoading = false;
  isNotFound = false;
  errorMessage: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private transactionApi: TransactionApiService
  ) {}

  ngOnInit(): void {
    const routeId = Number(this.route.snapshot.paramMap.get('transactionId'));

    if (!Number.isSafeInteger(routeId) || routeId < 1) {
      this.showNotFound();
      return;
    }

    this.transactionId = routeId;
    this.loadTransaction();
  }

  retry(): void {
    this.loadTransaction();
  }

  goBack(): void {
    this.router.navigate(['/history']);
  }

  amountLabel(transaction: TransactionDetail): string {
    const prefix = transaction.type === 'TOPUP' ? '+' : '-';
    return `${prefix}Rp ${this.formatNumber(transaction.amount)}`;
  }

  currencyLabel(value: number): string {
    return `Rp ${this.formatNumber(value)}`;
  }

  statusLabel(status: TransactionStatus): string {
    const labels: Record<TransactionStatus, string> = {
      SUCCESS: 'Berhasil',
      PENDING: 'Pending',
      FAILED: 'Gagal',
    };

    return labels[status];
  }

  typeLabel(type: TransactionType): string {
    const labels: Record<TransactionType, string> = {
      TOPUP: 'Topup',
      PAYMENT: 'Payment',
      TRANSFER: 'Transfer',
    };

    return labels[type];
  }

  statusClass(status: TransactionStatus): string {
    if (status === 'SUCCESS') {
      return 'bg-tertiary-container text-on-tertiary-container';
    }

    if (status === 'FAILED') {
      return 'bg-error-container text-on-error-container';
    }

    return 'bg-amber-100 text-amber-800';
  }

  iconFor(type: TransactionType): string {
    const icons: Record<TransactionType, string> = {
      TOPUP: 'wallet',
      PAYMENT: 'shopping_cart',
      TRANSFER: 'sync_alt',
    };

    return icons[type];
  }

  titleFor(transaction: TransactionDetail): string {
    if (transaction.type === 'TOPUP') {
      return 'Top-up Saldo';
    }

    if (transaction.type === 'PAYMENT') {
      return transaction.merchantName
        ? `Pembayaran ${transaction.merchantName}`
        : 'Pembayaran';
    }

    return 'Transfer Saldo';
  }

  timestampLabel(createdAt: string): string {
    return new Intl.DateTimeFormat('id-ID', {
      dateStyle: 'long',
      timeStyle: 'short',
    }).format(new Date(createdAt));
  }

  taxValueLabel(tax: TransactionTaxDetail): string {
    if (tax.valueType === 'PERCENTAGE') {
      return `${this.formatDecimal(tax.taxValue)}%`;
    }

    return this.currencyLabel(tax.taxValue);
  }

  private loadTransaction(): void {
    if (this.transactionId === null || this.isLoading) {
      return;
    }

    this.isLoading = true;
    this.isNotFound = false;
    this.errorMessage = null;

    this.transactionApi.getTransaction(this.transactionId)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.isLoading = false;
        })
      )
      .subscribe({
        next: (response) => {
          this.transaction = response.data;
        },
        error: (error: HttpErrorResponse) => {
          this.transaction = null;

          if (error.status === 404) {
            this.showNotFound();
            return;
          }

          this.errorMessage = 'Gagal memuat detail transaksi. Silakan coba lagi.';
        },
      });
  }

  private showNotFound(): void {
    this.isNotFound = true;
    this.transaction = null;
    this.errorMessage = 'Transaksi tidak ditemukan.';
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }

  private formatDecimal(value: number): string {
    return new Intl.NumberFormat('id-ID', {
      maximumFractionDigits: 4,
    }).format(value);
  }
}
