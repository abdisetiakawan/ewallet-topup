import { Routes } from '@angular/router';

export const GUEST_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./home/pages/home/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'login',
    loadComponent: () =>
      import('./auth/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./auth/pages/register/register.component').then((m) => m.RegisterComponent),
  },
];
