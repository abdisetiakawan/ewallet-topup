import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpRequest, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { TokenRefreshService } from '../services/token-refresh.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tokenRefresh = inject(TokenRefreshService);

  // Don't attach token to auth endpoints (login, register, refresh)
  if (isAuthRequest(req.url)) {
    return next(req);
  }

  const token = authService.getAccessToken();
  const authedReq = token ? addToken(req, token) : req;

  return next(authedReq).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        return tokenRefresh.handleUnauthorized(req, next, authService);
      }
      return throwError(() => error);
    })
  );
};

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
    withCredentials: true,
  });
}

function isAuthRequest(url: string): boolean {
  return (
    url.includes('/api/auth/login') ||
    url.includes('/api/auth/register') ||
    url.includes('/api/auth/refresh')
  );
}
