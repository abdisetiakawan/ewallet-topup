import { convertToParamMap } from '@angular/router';
import { of } from 'rxjs';
import { MerchantConfigComponent } from './merchant-config.component';
import { AdminMerchantDto } from '../../../../core/models/admin-merchant.model';

describe('MerchantConfigComponent', () => {
  it('sends the existing id when a tax is retagged and a new tax reuses the previous type', () => {
    const api = createApi();
    const component = createComponent(api);
    component.merchantId = 32;
    component.merchantName = 'Merchant';
    component.taxes = [
      {
        id: 6,
        taxName: 'Service Fee',
        taxType: 'SERVICE_FEE',
        valueType: 'PERCENTAGE',
        taxValue: 1.5,
        isActive: true,
        effectiveAt: '2026-05-22',
        expiredAt: '',
      },
      {
        id: null,
        taxName: 'Replacement Processing Fee',
        taxType: 'PROCESSING_FEE',
        valueType: 'FIXED',
        taxValue: 1500,
        isActive: true,
        effectiveAt: '2026-05-22',
        expiredAt: '',
      },
    ];

    component.save();

    expect(api.updateMerchant).toHaveBeenCalledWith(32, {
      name: 'Merchant',
      isActive: true,
      taxes: [
        {
          id: 6,
          taxName: 'Service Fee',
          taxType: 'SERVICE_FEE',
          valueType: 'PERCENTAGE',
          taxValue: 1.5,
          isActive: true,
          effectiveAt: '2026-05-22T00:00:00',
          expiredAt: null,
        },
        {
          id: null,
          taxName: 'Replacement Processing Fee',
          taxType: 'PROCESSING_FEE',
          valueType: 'FIXED',
          taxValue: 1500,
          isActive: true,
          effectiveAt: '2026-05-22T00:00:00',
          expiredAt: null,
        },
      ],
    });
  });

  it('blocks save when the final state contains duplicate active tax types', () => {
    const api = createApi();
    const component = createComponent(api);
    component.merchantId = 32;
    component.merchantName = 'Merchant';
    component.taxes = [
      {
        id: 6,
        taxName: 'Processing Fee',
        taxType: 'PROCESSING_FEE',
        valueType: 'FIXED',
        taxValue: 1000,
        isActive: true,
        effectiveAt: '2026-05-22',
        expiredAt: '',
      },
      {
        id: null,
        taxName: 'Duplicate Processing Fee',
        taxType: 'PROCESSING_FEE',
        valueType: 'FIXED',
        taxValue: 1500,
        isActive: true,
        effectiveAt: '2026-05-22',
        expiredAt: '',
      },
    ];

    component.save();

    expect(component.errorMessage).toBe('Tax type aktif tidak boleh duplikat.');
    expect(api.updateMerchant).not.toHaveBeenCalled();
  });

  function createComponent(api: ReturnType<typeof createApi>): MerchantConfigComponent {
    return new MerchantConfigComponent(
      { snapshot: { paramMap: convertToParamMap({ merchantId: '32' }) } } as any,
      { navigate: jasmine.createSpy('navigate') } as any,
      api as any,
      { markForCheck: jasmine.createSpy('markForCheck') } as any
    );
  }

  function createApi() {
    return {
      createMerchant: jasmine.createSpy('createMerchant').and.returnValue(of(response())),
      updateMerchant: jasmine.createSpy('updateMerchant').and.returnValue(of(response())),
    };
  }

  function response() {
    return {
      success: true,
      message: 'ok',
      data: {
        id: 32,
        name: 'Merchant',
        isActive: true,
        taxes: [],
      } satisfies AdminMerchantDto,
    };
  }
});
