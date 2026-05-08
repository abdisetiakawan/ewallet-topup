import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-bottom-nav-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bottom-nav-bar.component.html',
  styleUrl: './bottom-nav-bar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BottomNavBarComponent {
  @Input() activeNav: 'home' | 'wallets' | 'history' | 'account' = 'wallets';
}
