import { ChangeDetectionStrategy, Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-bottom-nav-bar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './bottom-nav-bar.component.html',
  styleUrl: './bottom-nav-bar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BottomNavBarComponent {
  @Input() activeNav: 'home' | 'wallets' | 'history' | 'account' = 'wallets';
}
