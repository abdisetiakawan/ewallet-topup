import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-payment-summary',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-summary.component.html',
  styleUrl: './payment-summary.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PaymentSummaryComponent {
  @Input() amount: number = 0;
  @Input() adminFee: number = 1000;
  @Input() variant: 'desktop' | 'mobile' = 'desktop';
  @Input() disabled: boolean = false;
  @Output() pay = new EventEmitter<void>();

  get total(): number {
    return this.amount + this.adminFee;
  }

  get isValid(): boolean {
    return !this.disabled && this.amount >= 10000;
  }

  format(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }

  onPay(): void {
    if (this.isValid) {
      this.pay.emit();
    }
  }
}
