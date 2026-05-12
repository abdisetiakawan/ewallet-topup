import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // If token is already in memory, allow access
  if (authService.isLoggedIn()) {
    return true;
  }

  // Token lost (e.g. page refresh) — try silent refresh via cookie
  return authService.refresh().pipe(
    map((success) => (success ? true : router.createUrlTree(['/login'])))
  );
};
