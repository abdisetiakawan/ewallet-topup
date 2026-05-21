import { CommonModule } from '@angular/common';
import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';
import {
  TransactionHistoryItem,
  TransactionStatus,
  TransactionType,
} from '../../../../core/models/transaction.model';
import { TransactionApiService } from '../../../../core/services/transaction-api.service';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';

type HistoryType = 'ALL' | TransactionType;
type HistorySort = 'NEWEST' | 'OLDEST';

interface HistoryFilter {
  label: string;
  value: HistoryType;
}

interface HistorySortOption {
  label: string;
  value: HistorySort;
  icon: string;
}

interface HistoryTransaction extends TransactionHistoryItem {
  title: string;
}

interface HistoryGroup {
  label: string;
  transactions: HistoryTransaction[];
}

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, RouterLink, BottomNavBarComponent, TopAppBarComponent],
  templateUrl: './history.component.html',
  styleUrl: './history.component.css',
})
export class HistoryComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);
  private readonly pageSize = 10;
  private requestSequence = 0;

  readonly filters: HistoryFilter[] = [
    { label: 'Semua', value: 'ALL' },
    { label: 'Topup', value: 'TOPUP' },
    { label: 'Payment', value: 'PAYMENT' },
  ];
  readonly sortOptions: HistorySortOption[] = [
    { label: 'Terbaru', value: 'NEWEST', icon: 'south' },
    { label: 'Terlama', value: 'OLDEST', icon: 'north' },
  ];
  readonly loadingRows = [1, 2, 3, 4];

  selectedType: HistoryType = 'ALL';
  selectedSort: HistorySort = 'NEWEST';
  searchTerm = '';
  transactions: HistoryTransaction[] = [];
  visibleGroups: HistoryGroup[] = [];
  isSortMenuOpen = false;
  isLoading = false;
  isLoadingMore = false;
  errorMessage: string | null = null;
  currentPage = 0;
  totalPages = 0;
  totalElements = 0;

  constructor(private transactionApi: TransactionApiService) {}

  ngOnInit(): void {
    this.loadTransactions();
  }

  get hasMoreTransactions(): boolean {
    return this.currentPage + 1 < this.totalPages;
  }

  selectType(type: HistoryType): void {
    if (this.selectedType === type) {
      return;
    }

    this.selectedType = type;
    this.searchTerm = '';
    this.loadTransactions(0, true);
  }

  onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchTerm = target.value;
    this.updateVisibleGroups();
  }

  toggleSortMenu(): void {
    this.isSortMenuOpen = !this.isSortMenuOpen;
  }

  closeSortMenu(): void {
    this.isSortMenuOpen = false;
  }

  selectSort(sort: HistorySort): void {
    this.selectedSort = sort;
    this.isSortMenuOpen = false;
    this.updateVisibleGroups();
  }

  isSelectedSort(sort: HistorySort): boolean {
    return this.selectedSort === sort;
  }

  retryLoadTransactions(): void {
    this.isSortMenuOpen = false;
    this.loadTransactions();
  }

  loadMoreTransactions(): void {
    if (!this.hasMoreTransactions || this.isLoadingMore) {
      return;
    }

    this.loadTransactions(this.currentPage + 1);
  }

  titleFor(transaction: HistoryTransaction): string {
    return transaction.title;
  }

  amountLabel(transaction: HistoryTransaction): string {
    const prefix = transaction.type === 'TOPUP' ? '+' : '-';
    return `${prefix}Rp ${this.formatNumber(transaction.amount)}`;
  }

  taxLabel(taxAmount: number): string {
    return `Rp ${this.formatNumber(taxAmount)}`;
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
      return 'text-tertiary';
    }

    if (status === 'FAILED') {
      return 'text-error';
    }

    return 'text-amber-600';
  }

  iconFor(type: TransactionType): string {
    const icons: Record<TransactionType, string> = {
      TOPUP: 'wallet',
      PAYMENT: 'shopping_cart',
      TRANSFER: 'sync_alt',
    };

    return icons[type];
  }

  iconClass(transaction: HistoryTransaction): string {
    if (transaction.status === 'FAILED') {
      return 'bg-error-container text-on-error-container';
    }

    return transaction.type === 'TOPUP'
      ? 'bg-primary-fixed text-on-primary-fixed-variant'
      : 'bg-secondary-container text-on-secondary-container';
  }

  amountClass(transaction: HistoryTransaction): string {
    return transaction.type === 'TOPUP' ? 'text-tertiary' : 'text-on-surface';
  }

  timeLabel(createdAt: string): string {
    return new Intl.DateTimeFormat('id-ID', {
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(createdAt));
  }

  private loadTransactions(page = 0, force = false): void {
    const isFirstPage = page === 0;

    if (!force && ((isFirstPage && this.isLoading) || (!isFirstPage && this.isLoadingMore))) {
      return;
    }

    const requestId = ++this.requestSequence;

    if (isFirstPage) {
      this.isLoading = true;
      this.isLoadingMore = false;
    } else {
      this.isLoadingMore = true;
    }

    this.errorMessage = null;

    this.transactionApi.getTransactions({
      page,
      size: this.pageSize,
      type: this.selectedType === 'ALL' ? undefined : this.selectedType,
    })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          if (requestId !== this.requestSequence) {
            return;
          }

          if (isFirstPage) {
            this.isLoading = false;
          } else {
            this.isLoadingMore = false;
          }
        })
      )
      .subscribe({
        next: (response) => {
          if (requestId !== this.requestSequence) {
            return;
          }

          const nextTransactions = response.data.content.map((transaction) =>
            this.toHistoryTransaction(transaction)
          );

          this.transactions = isFirstPage
            ? nextTransactions
            : [...this.transactions, ...nextTransactions];
          this.currentPage = response.data.page;
          this.totalPages = response.data.totalPages;
          this.totalElements = response.data.totalElements;
          this.updateVisibleGroups();
        },
        error: () => {
          if (requestId !== this.requestSequence) {
            return;
          }

          if (isFirstPage) {
            this.transactions = [];
            this.visibleGroups = [];
          }

          this.errorMessage = 'Gagal memuat riwayat transaksi. Silakan coba lagi.';
        },
      });
  }

  private toHistoryTransaction(transaction: TransactionHistoryItem): HistoryTransaction {
    return {
      ...transaction,
      title: this.createTitle(transaction),
    };
  }

  private createTitle(transaction: TransactionHistoryItem): string {
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

  private updateVisibleGroups(): void {
    const keyword = this.searchTerm.trim().toLowerCase();
    const filtered = this.transactions.filter((transaction) => {
      const matchesType = this.selectedType === 'ALL' || transaction.type === this.selectedType;
      const matchesKeyword = !keyword || this.matchesSearch(transaction, keyword);
      return matchesType && matchesKeyword;
    });

    this.visibleGroups = this.groupTransactions(this.sortTransactions(filtered));
  }

  private sortTransactions(transactions: HistoryTransaction[]): HistoryTransaction[] {
    return [...transactions].sort((left, right) => {
      if (this.selectedSort === 'OLDEST') {
        return this.timestamp(left.createdAt) - this.timestamp(right.createdAt);
      }

      return this.timestamp(right.createdAt) - this.timestamp(left.createdAt);
    });
  }

  private matchesSearch(transaction: HistoryTransaction, keyword: string): boolean {
    return [
      transaction.title,
      transaction.referenceId,
      transaction.description,
      transaction.merchantName,
      transaction.userName,
      transaction.userEmail,
      this.statusLabel(transaction.status),
      this.typeLabel(transaction.type),
    ]
      .filter(Boolean)
      .some((value) => value!.toLowerCase().includes(keyword));
  }

  private groupTransactions(transactions: HistoryTransaction[]): HistoryGroup[] {
    const groups = new Map<string, HistoryTransaction[]>();

    for (const transaction of transactions) {
      const label = this.groupLabel(transaction.createdAt);
      const items = groups.get(label) ?? [];
      items.push(transaction);
      groups.set(label, items);
    }

    return Array.from(groups.entries()).map(([label, items]) => ({
      label,
      transactions: items,
    }));
  }

  private groupLabel(createdAt: string): string {
    const date = new Date(createdAt);
    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(today.getDate() - 1);

    if (this.isSameDate(date, today)) {
      return 'Hari Ini';
    }

    if (this.isSameDate(date, yesterday)) {
      return 'Kemarin';
    }

    return new Intl.DateTimeFormat('id-ID', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }).format(date);
  }

  private isSameDate(left: Date, right: Date): boolean {
    return left.getFullYear() === right.getFullYear()
      && left.getMonth() === right.getMonth()
      && left.getDate() === right.getDate();
  }

  private timestamp(value: string): number {
    return new Date(value).getTime();
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }
}
