import { Routes } from '@angular/router';

export const CUSTOMER_ROUTES: Routes = [
  {
    path: 'topup',
    loadComponent: () =>
      import('./topup/pages/pilih-ewallet/pilih-ewallet.component').then(
        (m) => m.PilihEwalletComponent
      ),
  },
  {
    path: 'wallet/topup',
    loadComponent: () =>
      import('./topup/pages/halaman-topup/halaman-topup.component').then(
        (m) => m.HalamanTopupComponent
      ),
  },
  {
    path: 'payment/:merchantId',
    loadComponent: () =>
      import('./topup/pages/detail-pembayaran/detail-pembayaran.component').then(
        (m) => m.DetailPembayaranComponent
      ),
  },
  {
    path: 'account',
    loadComponent: () =>
      import('./profile/pages/profile-page/profile-page.component').then(
        (m) => m.ProfilePageComponent
      ),
  },
  {
    path: 'account/edit-profile',
    loadComponent: () =>
      import('./profile/pages/edit-profile/edit-profile.component').then(
        (m) => m.EditProfileComponent
      ),
  },
  {
    path: 'account/update-email',
    loadComponent: () =>
      import('./profile/pages/update-email/update-email.component').then(
        (m) => m.UpdateEmailComponent
      ),
  },
  {
    path: 'account/change-password',
    loadComponent: () =>
      import('./profile/pages/change-password/change-password.component').then(
        (m) => m.ChangePasswordComponent
      ),
  },
  {
    path: 'history',
    loadComponent: () =>
      import('./history/pages/history/history.component').then((m) => m.HistoryComponent),
  },
];
