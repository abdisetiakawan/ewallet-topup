import { ChangeDetectionStrategy, Component, Input } from '@angular/core';

@Component({
  selector: 'app-balance-card',
  standalone: true,
  imports: [],
  templateUrl: './balance-card.component.html',
  styleUrl: './balance-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BalanceCardComponent {
  @Input() balance: number = 12450000;
  @Input() variant: 'desktop' | 'mobile' = 'desktop';

  get formattedBalance(): string {
    return new Intl.NumberFormat('id-ID').format(this.balance);
  }
}
