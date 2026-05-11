import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, finalize, map, tap } from 'rxjs/operators';
import { BaseResponse } from '../models/auth.model';
import { PaymentRequest, PaymentResponse } from '../models/transaction.model';
import { TopupResponse } from '../models/wallet.model';
import { TransactionApiService } from './transaction-api.service';
import { WalletApiService } from './wallet-api.service';

@Injectable({
  providedIn: 'root',
})
export class WalletStoreService {
  private readonly balanceSubject = new BehaviorSubject<number>(0);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);
  private readonly errorSubject = new BehaviorSubject<string | null>(null);

  readonly balance$ = this.balanceSubject.asObservable();
  readonly loading$ = this.loadingSubject.asObservable();
  readonly error$ = this.errorSubject.asObservable();

  constructor(
    private walletApi: WalletApiService,
    private transactionApi: TransactionApiService
  ) {}

  get currentBalance(): number {
    return this.balanceSubject.value;
  }

  loadBalance(): Observable<number> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.walletApi.getBalance().pipe(
      map((response) => response.data.balance),
      tap((balance) => this.balanceSubject.next(balance)),
      catchError((error) => {
        this.errorSubject.next(this.resolveErrorMessage(error));
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  topup(amount: number): Observable<BaseResponse<TopupResponse>> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.walletApi.topup({ amount }).pipe(
      tap((response) => this.balanceSubject.next(response.data.balanceAfter)),
      catchError((error) => {
        this.errorSubject.next(this.resolveErrorMessage(error));
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
  }

  pay(payload: PaymentRequest): Observable<BaseResponse<PaymentResponse>> {
    this.loadingSubject.next(true);
    this.errorSubject.next(null);

    return this.transactionApi.pay(payload).pipe(
      tap((response) => this.balanceSubject.next(response.data.balanceAfter)),
      catchError((error) => {
        this.errorSubject.next(this.resolveErrorMessage(error));
        return throwError(() => error);
      }),
      finalize(() => this.loadingSubject.next(false))
    );
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

    return 'Gagal memproses saldo. Silakan coba lagi.';
  }
}
