import { ChangeDetectionStrategy, Component, inject, OnInit } from '@angular/core';
import { AsyncPipe, DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import { ProfileStoreService } from '../../../../core/services/profile-store.service';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [AsyncPipe, DatePipe, RouterLink, BottomNavBarComponent, TopAppBarComponent],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfilePageComponent implements OnInit {
  private profileStore = inject(ProfileStoreService);
  private authService = inject(AuthService);
  private router = inject(Router);

  readonly user$ = this.profileStore.user$;
  readonly loading$ = this.profileStore.loading$;

  ngOnInit(): void {
    this.profileStore.loadProfile().subscribe();
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }
}
