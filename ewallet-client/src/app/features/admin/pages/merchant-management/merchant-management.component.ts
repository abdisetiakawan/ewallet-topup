import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

interface AdminMerchantTax {
  taxName: string;
  taxType: 'PPN' | 'SERVICE_FEE' | 'PLATFORM_FEE' | 'ADMIN_FEE' | 'TRANSFER_FEE' | 'PROCESSING_FEE';
  valueType: 'PERCENTAGE' | 'FIXED';
  taxValue: number;
}

interface AdminMerchant {
  id: number;
  name: string;
  isActive: boolean;
  taxes: AdminMerchantTax[];
}

@Component({
  selector: 'app-merchant-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './merchant-management.component.html',
  styleUrl: './merchant-management.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MerchantManagementComponent {
  searchTerm = '';

  readonly merchants: AdminMerchant[] = [
    {
      id: 1,
      name: 'Gopay',
      isActive: true,
      taxes: [
        { taxName: 'Admin Fee', taxType: 'ADMIN_FEE', valueType: 'FIXED', taxValue: 1000 },
        { taxName: 'Service Fee', taxType: 'SERVICE_FEE', valueType: 'PERCENTAGE', taxValue: 1.5 },
      ],
    },
    {
      id: 2,
      name: 'OVO',
      isActive: true,
      taxes: [
        { taxName: 'Platform Fee', taxType: 'PLATFORM_FEE', valueType: 'FIXED', taxValue: 1500 },
      ],
    },
    {
      id: 3,
      name: 'LinkAja',
      isActive: false,
      taxes: [
        { taxName: 'Transfer Fee', taxType: 'TRANSFER_FEE', valueType: 'FIXED', taxValue: 2000 },
      ],
    },
  ];

  get filteredMerchants(): AdminMerchant[] {
    const keyword = this.searchTerm.trim().toLowerCase();

    if (!keyword) {
      return this.merchants;
    }

    return this.merchants.filter((merchant) => {
      const idLabel = `m-${merchant.id.toString().padStart(4, '0')}`;
      return merchant.name.toLowerCase().includes(keyword) || idLabel.includes(keyword);
    });
  }

  formatMerchantId(id: number): string {
    return `#M-${id.toString().padStart(4, '0')}`;
  }

  formatTaxSummary(merchant: AdminMerchant): string {
    if (merchant.taxes.length === 0) {
      return 'Belum ada tax aktif';
    }

    return merchant.taxes
      .map((tax) => `${this.formatTaxType(tax.taxType)} ${this.formatTaxValue(tax)}`)
      .join(' + ');
  }

  formatTaxType(type: AdminMerchantTax['taxType']): string {
    const labels: Record<AdminMerchantTax['taxType'], string> = {
      PPN: 'PPN',
      SERVICE_FEE: 'Service',
      PLATFORM_FEE: 'Platform',
      ADMIN_FEE: 'Admin',
      TRANSFER_FEE: 'Transfer',
      PROCESSING_FEE: 'Processing',
    };

    return labels[type];
  }

  private formatTaxValue(tax: AdminMerchantTax): string {
    if (tax.valueType === 'PERCENTAGE') {
      return `${tax.taxValue}%`;
    }

    return `Rp ${new Intl.NumberFormat('id-ID').format(tax.taxValue)}`;
  }
}
