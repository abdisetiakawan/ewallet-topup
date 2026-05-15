import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const customerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const routeForCurrentUser = () => {
    const role = authService.getCurrentUser()?.role;
    if (role === 'CUSTOMER') {
      return true;
    }

    if (role === 'ADMIN') {
      return router.createUrlTree(['/admin/merchants']);
    }

    authService.clearSession();
    return router.createUrlTree(['/login']);
  };

  if (authService.isLoggedIn()) {
    return routeForCurrentUser();
  }

  return authService.refresh().pipe(
    map((success) => {
      if (!success) return router.createUrlTree(['/login']);
      return routeForCurrentUser();
    })
  );
};
