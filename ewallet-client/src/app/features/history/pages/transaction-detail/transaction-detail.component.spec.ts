import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TransactionDetail } from '../../../../core/models/transaction.model';
import { TransactionApiService } from '../../../../core/services/transaction-api.service';
import { TransactionDetailComponent } from './transaction-detail.component';

describe('TransactionDetailComponent', () => {
  let transactionApi: jasmine.SpyObj<TransactionApiService>;

  const detail: TransactionDetail = {
    transactionId: 100,
    referenceId: 'PAY-TEST',
    amount: 102_500,
    baseAmount: 100_000,
    taxAmount: 2_500,
    balanceBefore: 200_000,
    balanceAfter: 97_500,
    type: 'PAYMENT',
    status: 'SUCCESS',
    description: 'Top-up Gopay',
    merchantName: 'Gopay',
    taxDetails: [
      {
        taxName: 'Service Fee',
        taxCategory: 'SERVICE_FEE',
        valueType: 'PERCENTAGE',
        taxValue: 1.5,
        calculatedAmount: 1_500,
      },
    ],
    createdAt: '2026-05-18T09:00:00',
  };

  beforeEach(async () => {
    transactionApi = jasmine.createSpyObj<TransactionApiService>('TransactionApiService', ['getTransaction']);
    transactionApi.getTransaction.and.returnValue(of({
      requestId: 'request-1',
      status: true,
      message: 'Transaction detail retrieved successfully',
      data: detail,
    }));

    await TestBed.configureTestingModule({
      imports: [TransactionDetailComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        { provide: TransactionApiService, useValue: transactionApi },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ transactionId: '100' }),
            },
          },
        },
      ],
    }).compileComponents();
  });

  it('loads and renders the detail route payload', () => {
    const fixture = TestBed.createComponent(TransactionDetailComponent);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(transactionApi.getTransaction).toHaveBeenCalledWith(100);
    expect(text).toContain('Pembayaran Gopay');
    expect(text).toContain('PAY-TEST');
    expect(text).toContain('Service Fee');
    expect(text).toContain('Audit Saldo');
  });

  it('shows a not found state when the API returns 404', () => {
    transactionApi.getTransaction.and.returnValue(throwError(() => new HttpErrorResponse({ status: 404 })));

    const fixture = TestBed.createComponent(TransactionDetailComponent);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(text).toContain('Detail tidak tersedia');
    expect(text).toContain('Transaksi tidak ditemukan.');
  });
});
