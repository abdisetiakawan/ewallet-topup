import { ChangeDetectionStrategy, Component, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-bottom-nav-bar',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './bottom-nav-bar.component.html',
  styleUrl: './bottom-nav-bar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BottomNavBarComponent {
  private authService = inject(AuthService);

  @Input() activeNav: 'home' | 'wallets' | 'history' | 'account' = 'wallets';

  isAdmin(): boolean {
    return this.authService.getCurrentUser()?.role === 'ADMIN';
  }
}
