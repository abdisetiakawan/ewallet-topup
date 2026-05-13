import { Injectable } from '@angular/core';
import { MerchantDto } from '../models/merchant.model';
import { EWallet } from '../models/ewallet.model';

/**
 * Single source of truth untuk mapping MerchantDto → EWallet.
 * Menyatukan uiMetadata dan tax calculation logic yang sebelumnya
 * terduplikasi di PilihEwalletComponent dan DetailPembayaranComponent.
 */
@Injectable({
  providedIn: 'root',
})
export class MerchantMapperService {
  private readonly UI_METADATA: Record<string, Partial<EWallet>> = {
    'gopay': { id: 'gopay', icon: 'payments', iconBgColor: '#e5eeff', iconTextColor: '#0058be', featured: true },
    'ovo': { id: 'ovo', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA' },
    'dana': { id: 'dana', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be' },
    'shopeepay': { id: 'shopeepay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D' },
    'linkaja': { id: 'linkaja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a' },
  };

  private readonly DEFAULT_META: Required<Pick<EWallet, 'icon' | 'iconBgColor' | 'iconTextColor'>> = {
    icon: 'account_balance_wallet',
    iconBgColor: '#f3f4f6',
    iconTextColor: '#374151',
  };

  mapToEWallet(merchant: MerchantDto): EWallet {
    const lowerName = merchant.name.toLowerCase();
    const meta = this.UI_METADATA[lowerName] ?? {};
    const { feeValue, feeType, feeLabel } = this.resolveFee(merchant);

    return {
      id: meta.id ?? lowerName,
      name: merchant.name,
      icon: meta.icon ?? this.DEFAULT_META.icon,
      iconBgColor: meta.iconBgColor ?? this.DEFAULT_META.iconBgColor,
      iconTextColor: meta.iconTextColor ?? this.DEFAULT_META.iconTextColor,
      feeValue,
      feeType,
      feeLabel,
      featured: meta.featured,
    };
  }

  mapAllToEWallets(merchants: MerchantDto[]): EWallet[] {
    return merchants.map((merchant) => this.mapToEWallet(merchant));
  }

  private resolveFee(merchant: MerchantDto): {
    feeValue: number;
    feeType: 'FIXED' | 'PERCENTAGE';
    feeLabel: string;
  } {
    let totalFixed = 0;
    let totalPercentage = 0;

    for (const tax of merchant.taxes) {
      if (tax.valueType === 'FIXED') totalFixed += tax.taxValue;
      if (tax.valueType === 'PERCENTAGE') totalPercentage += tax.taxValue;
    }

    if (totalPercentage > 0) {
      return {
        feeValue: totalPercentage,
        feeType: 'PERCENTAGE',
        feeLabel: `Biaya ${totalPercentage}%`,
      };
    }

    if (totalFixed > 0) {
      return {
        feeValue: totalFixed,
        feeType: 'FIXED',
        feeLabel: `Biaya Rp ${new Intl.NumberFormat('id-ID').format(totalFixed)}`,
      };
    }

    return { feeValue: 0, feeType: 'FIXED', feeLabel: 'Bebas biaya' };
  }
}
