import { ChangeDetectionStrategy, Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [RouterLink, BottomNavBarComponent, TopAppBarComponent],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePageComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
