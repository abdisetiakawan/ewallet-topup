import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AmountOption } from '../../../../core/models/topup.model';

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

  readonly presetAmounts: AmountOption[] = [
    { value: 50000, label: 'Rp 50.000' },
    { value: 100000, label: 'Rp 100.000' },
    { value: 200000, label: 'Rp 200.000' },
    { value: 500000, label: 'Rp 500.000' },
  ];

  manualInput: string = '';

  selectPreset(amount: number): void {
    this.manualInput = '';
    this.amountChange.emit(amount);
  }

  onManualInput(value: string): void {
    const numeric = parseInt(value.replace(/\D/g, ''), 10);
    this.amountChange.emit(isNaN(numeric) ? 0 : numeric);
  }
}
