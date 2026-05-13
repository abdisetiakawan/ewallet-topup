import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/home/pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/pages/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'topup',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/topup/pages/pilih-ewallet/pilih-ewallet.component').then(
        (m) => m.PilihEwalletComponent
      ),
  },
  {
    path: 'wallet/topup',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/topup/pages/halaman-topup/halaman-topup.component').then(
        (m) => m.HalamanTopupComponent
      ),
  },
  {
    path: 'payment/:merchantId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/topup/pages/detail-pembayaran/detail-pembayaran.component').then(
        (m) => m.DetailPembayaranComponent
      ),
  },
  {
    path: 'account',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/pages/profile-page/profile-page.component').then(
        (m) => m.ProfilePageComponent
      ),
  },
  {
    path: 'account/edit-profile',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/profile/pages/edit-profile/edit-profile.component').then(
        (m) => m.EditProfileComponent
      ),
  },
  {
    path: 'history',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/history/pages/history/history.component').then(
        (m) => m.HistoryComponent
      ),
  },
  {
    path: 'admin/merchants',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/admin/pages/merchant-management/merchant-management.component').then(
        (m) => m.MerchantManagementComponent
      ),
  },
  {
    path: 'admin/merchants/:merchantId/config',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/admin/pages/merchant-config/merchant-config.component').then(
        (m) => m.MerchantConfigComponent
      ),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
