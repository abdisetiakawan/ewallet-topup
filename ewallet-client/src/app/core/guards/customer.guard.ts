import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { map } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const customerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isCustomer = () => authService.getCurrentUser()?.role === 'CUSTOMER';

  if (authService.isLoggedIn()) {
    return isCustomer() ? true : router.createUrlTree(['/admin/merchants']);
  }

  return authService.refresh().pipe(
    map((success) => {
      if (!success) return router.createUrlTree(['/login']);
      return isCustomer() ? true : router.createUrlTree(['/admin/merchants']);
    })
  );
};
