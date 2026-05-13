import { Injectable } from '@angular/core';
import { HttpHandlerFn, HttpRequest, HttpErrorResponse, HttpEvent } from '@angular/common/http';
import { BehaviorSubject, Observable, catchError, filter, switchMap, take, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/**
 * Mengelola state token refresh dan antrian request yang menunggu.
 *
 * Dipindahkan dari module-level variable di auth.interceptor ke service
 * agar scoped ke Angular DI (providedIn: 'root'), sehingga:
 *
 * - Browser SPA: singleton per app instance — behavior identik dengan sebelumnya.
 * - Angular SSR: setiap request mendapat injector context baru → service di-instantiate
 *   ulang per request → tidak ada shared state antar concurrent SSR requests.
 * - Unit test: bisa di-reset antar test case via DI, tidak ada state leakage.
 */
@Injectable({
  providedIn: 'root',
})
export class TokenRefreshService {
  /**
   * Sentinel value yang dikirim ke `refreshSubject$` saat refresh gagal.
   * Membuat semua request yang antri langsung fail dengan 401 daripada hang
   * selamanya karena `filter(token => token !== null)` tidak pernah terpenuhi.
   */
  private readonly REFRESH_FAILED = '__REFRESH_FAILED__';

  private isRefreshing = false;
  private readonly refreshSubject$ = new BehaviorSubject<string | null>(null);

  /**
   * Dipanggil oleh authInterceptor ketika menerima 401.
   * Menangani dua skenario:
   *   1. Belum ada refresh berjalan → mulai refresh, queue request ini.
   *   2. Refresh sedang berjalan → queue request sampai token baru tersedia.
   */
  handleUnauthorized(
    req: HttpRequest<unknown>,
    next: HttpHandlerFn,
    authService: AuthService
  ): Observable<HttpEvent<unknown>> {
    if (!this.isRefreshing) {
      return this.startRefresh(req, next, authService);
    }

    return this.waitForRefresh(req, next);
  }

  /**
   * Reset state refresh ke kondisi awal.
   * Dipanggil saat logout agar `refreshSubject$` tidak menyimpan
   * nilai REFRESH_FAILED yang bisa meracuni request setelah login ulang.
   */
  reset(): void {
    this.isRefreshing = false;
    this.refreshSubject$.next(null);
  }

  private startRefresh(
    req: HttpRequest<unknown>,
    next: HttpHandlerFn,
    authService: AuthService
  ): Observable<HttpEvent<unknown>> {
    this.isRefreshing = true;
    this.refreshSubject$.next(null);

    return authService.refresh().pipe(
      switchMap((success) => {
        this.isRefreshing = false;

        if (success) {
          const newToken = authService.getAccessToken()!;
          this.refreshSubject$.next(newToken);
          return next(this.addToken(req, newToken));
        }

        // Refresh berhasil dipanggil tapi server menolak (misal cookie expired)
        this.refreshSubject$.next(this.REFRESH_FAILED);
        return throwError(() => new HttpErrorResponse({ status: 401 }));
      }),
      catchError((err) => {
        this.isRefreshing = false;
        // Emit sentinel agar semua request yang antri tidak hang indefinitely
        this.refreshSubject$.next(this.REFRESH_FAILED);
        return throwError(() => err);
      })
    );
  }

  private waitForRefresh(
    req: HttpRequest<unknown>,
    next: HttpHandlerFn
  ): Observable<HttpEvent<unknown>> {
    return this.refreshSubject$.pipe(
      filter((token) => token !== null),
      take(1),
      switchMap((token) => {
        if (token === this.REFRESH_FAILED) {
          return throwError(() => new HttpErrorResponse({ status: 401 }));
        }
        return next(this.addToken(req, token!));
      })
    );
  }

  private addToken(req: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
    return req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
      withCredentials: true,
    });
  }
}
