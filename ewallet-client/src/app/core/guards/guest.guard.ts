import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { map } from 'rxjs';

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const redirectAfterLogin = () => {
    const role = authService.getCurrentUser()?.role;
    if (role === 'ADMIN') {
      return router.createUrlTree(['/admin/merchants']);
    }

    if (role === 'CUSTOMER') {
      return router.createUrlTree(['/topup']);
    }

    authService.clearSession();
    return true;
  };

  if (authService.isLoggedIn()) {
    return redirectAfterLogin();
  }

  // Try silent refresh — if cookie is still valid, user is still logged in
  return authService.refresh().pipe(
    map((success) => (success ? redirectAfterLogin() : true))
  );
};
