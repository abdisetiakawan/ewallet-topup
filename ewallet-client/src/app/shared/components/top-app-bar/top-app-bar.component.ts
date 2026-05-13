import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-top-app-bar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './top-app-bar.component.html',
  styleUrl: './top-app-bar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TopAppBarComponent {
  @Input() balance: number = 0;
  @Input() showNavLinks: boolean = true;
  @Input() activeNav: 'home' | 'wallets' | 'history' = 'wallets';

  get formattedBalance(): string {
    return new Intl.NumberFormat('id-ID').format(this.balance);
  }
}
