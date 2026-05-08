import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/home/page/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
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
    path: 'topup/detail/:walletId',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/topup/pages/detail-pembayaran/detail-pembayaran.component').then(
        (m) => m.DetailPembayaranComponent
      ),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
