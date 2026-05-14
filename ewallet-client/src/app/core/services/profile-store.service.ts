import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';
import { UserSummary } from '../models/auth.model';
import { AuthService } from './auth.service';
import { UpdateProfileRequest, UserApiService } from './user-api.service';

@Injectable({
  providedIn: 'root',
})
export class ProfileStoreService {
  private readonly userSubject = new BehaviorSubject<UserSummary | null>(null);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly errorSubject = new BehaviorSubject<string | null>(null);

  readonly user$ = this.userSubject.asObservable();
  readonly loading$ = this.loadingSubject.asObservable();
  readonly error$ = this.errorSubject.asObservable();

  get currentUser(): UserSummary | null {
    return this.userSubject.value;
  }

  constructor(
    private userApi: UserApiService,
    private authService: AuthService
  ) {
    // Seed from cache immediately so UI shows data without waiting for the API
    this.userSubject.next(this.authService.getCurrentUser());
  }

  loadProfile(): Observable<UserSummary> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.userApi.getProfile().pipe(
      map((response) => response.data),
      tap((user) => {
        this.userSubject.next(user);
        this.authService.saveUser(user);
      }),
      catchError((error) => {
        this.errorSubject.next(this.resolveErrorMessage(error));
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  updateProfile(payload: UpdateProfileRequest): Observable<UserSummary> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.userApi.updateProfile(payload).pipe(
      map((response) => response.data),
      tap((user) => {
        this.userSubject.next(user);
        this.authService.saveUser(user);
      }),
      catchError((error) => {
        this.errorSubject.next(this.resolveErrorMessage(error));
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  /** Langsung inject data user baru ke store (setelah email/nama berubah dari flow eksternal) */
  applyUser(user: UserSummary): void {
    this.userSubject.next(user);
    this.authService.saveUser(user);
  }

  clearError(): void {
    this.errorSubject.next(null);
  }

  private resolveErrorMessage(error: unknown): string {
    if (
      typeof error === 'object' &&
      error !== null &&
      'error' in error &&
      typeof (error as { error?: { message?: unknown } }).error?.message === 'string'
    ) {
      return (error as { error: { message: string } }).error.message;
    }

    return 'Gagal memuat profil. Silakan coba lagi.';
  }
}
