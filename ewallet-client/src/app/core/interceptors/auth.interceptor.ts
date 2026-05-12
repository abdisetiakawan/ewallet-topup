import { inject } from '@angular/core';
import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { AuthService } from '../services/auth.service';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError } from 'rxjs';

let isRefreshing = false;
const refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  // Don't attach token to auth endpoints (login, register, refresh)
  if (isAuthRequest(req.url)) {
    return next(req);
  }

  const token = authService.getAccessToken();
  const authedReq = token ? addToken(req, token) : req;

  return next(authedReq).pipe(
    catchError((error) => {
      if (error instanceof HttpErrorResponse && error.status === 401) {
        return handle401(req, next, authService);
      }
      return throwError(() => error);
    })
  );
};

function handle401(req: HttpRequest<unknown>, next: HttpHandlerFn, authService: AuthService) {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refresh().pipe(
      switchMap((success) => {
        isRefreshing = false;

        if (success) {
          const newToken = authService.getAccessToken()!;
          refreshTokenSubject.next(newToken);
          return next(addToken(req, newToken));
        }

        refreshTokenSubject.next(null);
        return throwError(() => new HttpErrorResponse({ status: 401 }));
      }),
      catchError((err) => {
        isRefreshing = false;
        refreshTokenSubject.next(null);
        return throwError(() => err);
      })
    );
  }

  // Queue other requests while refreshing
  return refreshTokenSubject.pipe(
    filter((token) => token !== null),
    take(1),
    switchMap((token) => next(addToken(req, token!)))
  );
}

function addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return req.clone({
    setHeaders: { Authorization: `Bearer ${token}` },
    withCredentials: true,
  });
}

function isAuthRequest(url: string): boolean {
  return url.includes('/api/auth/login') ||
         url.includes('/api/auth/register') ||
         url.includes('/api/auth/refresh');
}
