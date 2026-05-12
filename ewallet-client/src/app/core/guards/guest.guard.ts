import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If already logged in (token in memory), redirect to main page
  if (authService.isLoggedIn()) {
    return router.createUrlTree(['/topup']);
  }

  // Try silent refresh — if cookie is still valid, user is still logged in
  return authService.refresh().pipe(
    map((success) => (success ? router.createUrlTree(['/topup']) : true))
  );
};
