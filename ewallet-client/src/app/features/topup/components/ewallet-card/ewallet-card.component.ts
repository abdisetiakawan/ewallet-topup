import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { EWallet } from '../../../../core/models/ewallet.model';

@Component({
  selector: 'app-ewallet-card',
  standalone: true,
  imports: [],
  templateUrl: './ewallet-card.component.html',
  styleUrl: './ewallet-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EwalletCardComponent {
  @Input() wallet!: EWallet;
  @Input() variant: 'desktop' | 'mobile' = 'desktop';
  @Output() selected = new EventEmitter<EWallet>();

  onSelect(): void {
    this.selected.emit(this.wallet);
  }
}
