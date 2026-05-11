import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

interface AmountOption {
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
export class HalamanTopupComponent {
  readonly balance = 1240500;
  selectedAmount = 100000;
  customAmount = '100.000';
  showConfirmationModal = false;

  readonly amountOptions: AmountOption[] = [
    { id: '50k', label: 'Rp 50rb', amount: 50000, badge: 'Hemat' },
    { id: '100k', label: 'Rp 100rb', amount: 100000, badge: 'Populer' },
    { id: '200k', label: 'Rp 200rb', amount: 200000, badge: 'Standar' },
    { id: '500k', label: 'Rp 500rb', amount: 500000, badge: 'Maksimal' },
  ];

  constructor(private router: Router) {}

  get formattedBalance(): string {
    return new Intl.NumberFormat('id-ID').format(this.balance);
  }

  get formattedSelectedAmount(): string {
    return new Intl.NumberFormat('id-ID').format(this.selectedAmount);
  }

  get formattedBalanceAfterTopup(): string {
    return new Intl.NumberFormat('id-ID').format(this.balance + this.selectedAmount);
  }

  get isValidAmount(): boolean {
    return this.selectedAmount >= 10000;
  }

  isSelectedAmount(amount: number): boolean {
    return this.selectedAmount === amount;
  }

  selectAmount(amount: number): void {
    this.selectedAmount = amount;
    this.customAmount = new Intl.NumberFormat('id-ID').format(amount);
  }

  onCustomAmountChange(value: string): void {
    this.customAmount = value;
    const amount = Number(value.replace(/\D/g, ''));

    if (Number.isFinite(amount)) {
      this.selectedAmount = amount;
    }
  }

  formatCustomAmount(): void {
    if (this.selectedAmount > 0) {
      this.customAmount = new Intl.NumberFormat('id-ID').format(this.selectedAmount);
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
    this.showConfirmationModal = false;
    alert(`Top-up saldo Rp ${this.formattedSelectedAmount} berhasil diproses.`);
  }

  goBack(): void {
    this.router.navigate(['/topup']);
  }
}
