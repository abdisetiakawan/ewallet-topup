import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AmountOption } from '../../../../core/models/topup.model';
import {
  MAX_PAYMENT_AMOUNT,
  MIN_TRANSACTION_AMOUNT,
  parseTransactionAmount,
} from '../../../../core/constants/transaction-limits';

@Component({
  selector: 'app-amount-selector',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './amount-selector.component.html',
  styleUrl: './amount-selector.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AmountSelectorComponent {
  @Input() selectedAmount: number = 0;
  @Input() variant: 'desktop' | 'mobile' = 'desktop';
  @Output() amountChange = new EventEmitter<number>();
  private readonly amountFormatter = new Intl.NumberFormat('id-ID');
  readonly limitHint = `Minimal Rp ${this.formatAmount(MIN_TRANSACTION_AMOUNT)}, maksimal Rp ${this.formatAmount(MAX_PAYMENT_AMOUNT)}`;
  manualAmountError: string | null = null;

  readonly presetAmounts: AmountOption[] = [
    { value: 50000, label: 'Rp 50.000' },
    { value: 100000, label: 'Rp 100.000' },
    { value: 200000, label: 'Rp 200.000' },
    { value: 500000, label: 'Rp 500.000' },
  ];

  manualInput: string = '';

  selectPreset(amount: number): void {
    this.manualInput = '';
    this.manualAmountError = null;
    this.amountChange.emit(amount);
  }

  onManualInput(value: string): void {
    const parsedAmount = parseTransactionAmount(value, MAX_PAYMENT_AMOUNT);
    this.manualInput = parsedAmount.displayValue;
    this.manualAmountError = parsedAmount.exceedsLimit
      ? `Maksimal payment Rp ${this.formatAmount(MAX_PAYMENT_AMOUNT)}`
      : null;
    this.amountChange.emit(parsedAmount.amount);
  }

  private formatAmount(amount: number): string {
    return this.amountFormatter.format(amount);
  }
}
