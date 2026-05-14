import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: 'merchants',
    loadComponent: () =>
      import('./pages/merchant-management/merchant-management.component').then(
        (m) => m.MerchantManagementComponent
      ),
  },
  {
    path: 'merchants/:merchantId/config',
    loadComponent: () =>
      import('./pages/merchant-config/merchant-config.component').then(
        (m) => m.MerchantConfigComponent
      ),
  },
];
