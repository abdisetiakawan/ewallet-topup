import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';

type HistoryType = 'ALL' | 'TOPUP' | 'PAYMENT';
type HistoryStatus = 'SUCCESS' | 'PENDING' | 'FAILED';

interface HistoryFilter {
  label: string;
  value: HistoryType;
}

interface HistoryTransaction {
  transactionId: number;
  title: string;
  referenceId: string;
  amount: number;
  taxAmount: number;
  type: Exclude<HistoryType, 'ALL'>;
  status: HistoryStatus;
  description: string | null;
  merchantName: string | null;
  createdAt: string;
}

interface HistoryGroup {
  label: string;
  transactions: HistoryTransaction[];
}

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, RouterLink, BottomNavBarComponent],
  templateUrl: './history.component.html',
  styleUrl: './history.component.css',
})
export class HistoryComponent {
  readonly filters: HistoryFilter[] = [
    { label: 'Semua', value: 'ALL' },
    { label: 'Topup', value: 'TOPUP' },
    { label: 'Payment', value: 'PAYMENT' },
  ];

  selectedType: HistoryType = 'ALL';
  searchTerm = '';

  readonly transactions: HistoryTransaction[] = [
    {
      transactionId: 1,
      title: 'Top-up Saldo',
      referenceId: 'TOPUP-DEMO-001',
      amount: 500000,
      taxAmount: 0,
      type: 'TOPUP',
      status: 'SUCCESS',
      description: 'Top-up saldo WalletPay',
      merchantName: null,
      createdAt: this.createDate(14, 30),
    },
    {
      transactionId: 2,
      title: 'Pembayaran Tokopedia',
      referenceId: 'PAY-DEMO-001',
      amount: 250000,
      taxAmount: 2500,
      type: 'PAYMENT',
      status: 'SUCCESS',
      description: 'Belanja kebutuhan bulanan',
      merchantName: 'Tokopedia',
      createdAt: this.createDate(10, 15),
    },
    {
      transactionId: 3,
      title: 'Pembayaran PLN',
      referenceId: 'PAY-DEMO-002',
      amount: 450000,
      taxAmount: 0,
      type: 'PAYMENT',
      status: 'PENDING',
      description: 'Tagihan listrik',
      merchantName: 'PLN',
      createdAt: this.createDate(9, 0, 1),
    },
    {
      transactionId: 4,
      title: 'Top-up Saldo',
      referenceId: 'TOPUP-DEMO-002',
      amount: 100000,
      taxAmount: 0,
      type: 'TOPUP',
      status: 'FAILED',
      description: 'Top-up saldo WalletPay',
      merchantName: null,
      createdAt: this.createDate(18, 45, 1),
    },
  ];

  constructor() {}

  get visibleGroups(): HistoryGroup[] {
    const keyword = this.searchTerm.trim().toLowerCase();
    const filtered = this.transactions.filter((t) => {
      const matchesType = this.selectedType === 'ALL' || t.type === this.selectedType;
      const matchesKeyword = !keyword || this.matchesSearch(t, keyword);
      return matchesType && matchesKeyword;
    });

    return this.groupTransactions(filtered);
  }

  selectType(type: HistoryType): void {
    this.selectedType = type;
  }

  onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchTerm = target.value;
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

  statusLabel(status: HistoryStatus): string {
    const labels: Record<HistoryStatus, string> = {
      SUCCESS: 'Berhasil',
      PENDING: 'Pending',
      FAILED: 'Gagal',
    };

    return labels[status];
  }

  typeLabel(type: Exclude<HistoryType, 'ALL'>): string {
    return type === 'TOPUP' ? 'Topup' : 'Payment';
  }

  statusClass(status: HistoryStatus): string {
    if (status === 'SUCCESS') {
      return 'text-tertiary';
    }

    if (status === 'FAILED') {
      return 'text-error';
    }

    return 'text-amber-600';
  }

  iconFor(type: Exclude<HistoryType, 'ALL'>): string {
    return type === 'TOPUP' ? 'wallet' : 'shopping_cart';
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



  private matchesSearch(transaction: HistoryTransaction, keyword: string): boolean {
    return [
      transaction.title,
      transaction.referenceId,
      transaction.description,
      transaction.merchantName,
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
      groups.set(label, [...(groups.get(label) ?? []), transaction]);
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

  private createDate(hour: number, minute: number, daysAgo = 0): string {
    const date = new Date();
    date.setDate(date.getDate() - daysAgo);
    date.setHours(hour, minute, 0, 0);

    return date.toISOString();
  }

  private formatNumber(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }
}
