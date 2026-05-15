import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const routeForCurrentUser = () => {
    const role = authService.getCurrentUser()?.role;
    if (role === 'ADMIN') {
      return true;
    }

    if (role === 'CUSTOMER') {
      return router.createUrlTree(['/topup']);
    }

    authService.clearSession();
    return router.createUrlTree(['/login']);
  };

  if (authService.isLoggedIn()) {
    return routeForCurrentUser();
  }

  return authService.refresh().pipe(
    map((success) => (success ? routeForCurrentUser() : router.createUrlTree(['/login'])))
  );
};
