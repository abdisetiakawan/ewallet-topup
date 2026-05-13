import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const hasAdminRole = () => authService.getCurrentUser()?.role === 'ADMIN';

  if (authService.isLoggedIn()) {
    return hasAdminRole() ? true : router.createUrlTree(['/topup']);
  }

  return authService.refresh().pipe(
    map((success) => (success && hasAdminRole() ? true : router.createUrlTree(['/login'])))
  );
};
