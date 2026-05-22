import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';
import { AuthService } from '../../../../core/services/auth.service';
import { MerchantApiService } from '../../../../core/services/merchant-api.service';
import { TransactionApiService } from '../../../../core/services/transaction-api.service';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { DetailPembayaranComponent } from './detail-pembayaran.component';

describe('DetailPembayaranComponent', () => {
  let authService: jasmine.SpyObj<AuthService>;
  let merchantApi: jasmine.SpyObj<MerchantApiService>;
  let transactionApi: jasmine.SpyObj<TransactionApiService>;
  let walletStore: jasmine.SpyObj<WalletStoreService>;

  beforeEach(async () => {
    authService = jasmine.createSpyObj<AuthService>('AuthService', ['getCurrentUser']);
    merchantApi = jasmine.createSpyObj<MerchantApiService>('MerchantApiService', ['getAllMerchants']);
    transactionApi = jasmine.createSpyObj<TransactionApiService>('TransactionApiService', ['quotePayment']);
    walletStore = jasmine.createSpyObj<WalletStoreService>(
      'WalletStoreService',
      ['loadBalance', 'pay'],
      { balance$: of(200_000) }
    );

    authService.getCurrentUser.and.returnValue({
      id: 1,
      name: 'Budi',
      email: 'budi@example.test',
      createdAt: '2026-05-22T00:00:00',
      role: 'CUSTOMER',
    });
    walletStore.loadBalance.and.returnValue(of(200_000));
    merchantApi.getAllMerchants.and.returnValue(of({
      requestId: 'request-merchant',
      status: true,
      message: 'Merchants retrieved successfully',
      data: [{
        id: 1,
        name: 'Gopay',
        isActive: true,
        taxes: [
          { taxName: 'Admin Fee', taxType: 'ADMIN_FEE', valueType: 'FIXED', taxValue: 1000 },
          { taxName: 'Service Fee', taxType: 'SERVICE_FEE', valueType: 'PERCENTAGE', taxValue: 1.5 },
        ],
      }],
    }));
    transactionApi.quotePayment.and.returnValue(of({
      requestId: 'request-quote',
      status: true,
      message: 'Payment quote calculated successfully',
      data: {
        merchantName: 'Gopay',
        baseAmount: 100_000,
        taxAmount: 2_500,
        amount: 102_500,
        taxDetails: [
          {
            taxName: 'Admin Fee',
            taxCategory: 'ADMIN_FEE',
            valueType: 'FIXED',
            taxValue: 1000,
            calculatedAmount: 1000,
          },
          {
            taxName: 'Service Fee',
            taxCategory: 'SERVICE_FEE',
            valueType: 'PERCENTAGE',
            taxValue: 1.5,
            calculatedAmount: 1500,
          },
        ],
      },
    }));

    await TestBed.configureTestingModule({
      imports: [DetailPembayaranComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authService },
        { provide: MerchantApiService, useValue: merchantApi },
        { provide: TransactionApiService, useValue: transactionApi },
        { provide: WalletStoreService, useValue: walletStore },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ merchantId: 'gopay' }),
            },
          },
        },
      ],
    }).compileComponents();
  });

  it('renders every tax detail from the server quote', fakeAsync(() => {
    const fixture = TestBed.createComponent(DetailPembayaranComponent);
    fixture.detectChanges();

    fixture.componentInstance.onAmountChange(100_000);
    tick(250);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(transactionApi.quotePayment).toHaveBeenCalledWith({
      merchantName: 'Gopay',
      amount: 100_000,
    });
    expect(fixture.componentInstance.totalPaymentAmount).toBe(102_500);
    expect(text).toContain('Admin Fee');
    expect(text).toContain('Service Fee');
    expect(text).toContain('102.500');
  }));

  it('does not request a quote until the amount is valid', fakeAsync(() => {
    const fixture = TestBed.createComponent(DetailPembayaranComponent);
    fixture.detectChanges();

    fixture.componentInstance.onAmountChange(1000);
    tick(250);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';

    expect(transactionApi.quotePayment).not.toHaveBeenCalled();
    expect(fixture.componentInstance.hasCurrentPaymentQuote).toBeFalse();
    expect(text).toContain('Pilih nominal valid untuk melihat rincian pajak.');
  }));
});
