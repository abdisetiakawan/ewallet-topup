import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

type TaxType = 'PPN' | 'SERVICE_FEE' | 'PLATFORM_FEE' | 'ADMIN_FEE' | 'TRANSFER_FEE' | 'PROCESSING_FEE';
type TaxValueType = 'PERCENTAGE' | 'FIXED';

interface EditableTax {
  id: number;
  taxName: string;
  taxType: TaxType;
  valueType: TaxValueType;
  taxValue: number;
  isActive: boolean;
  effectiveAt: string;
  expiredAt: string;
}

@Component({
  selector: 'app-merchant-config',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './merchant-config.component.html',
  styleUrl: './merchant-config.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MerchantConfigComponent {
  merchantName = 'Gopay';
  isMerchantActive = true;

  readonly taxTypeOptions: { value: TaxType; label: string }[] = [
    { value: 'PPN', label: 'PPN' },
    { value: 'ADMIN_FEE', label: 'Admin Fee' },
    { value: 'SERVICE_FEE', label: 'Service Fee' },
    { value: 'PLATFORM_FEE', label: 'Platform Fee' },
    { value: 'TRANSFER_FEE', label: 'Transfer Fee' },
    { value: 'PROCESSING_FEE', label: 'Processing Fee' },
  ];

  taxes: EditableTax[] = [
    {
      id: 1,
      taxName: 'Admin Fee',
      taxType: 'ADMIN_FEE',
      valueType: 'FIXED',
      taxValue: 1000,
      isActive: true,
      effectiveAt: '2026-01-01',
      expiredAt: '',
    },
    {
      id: 2,
      taxName: 'Service Fee',
      taxType: 'SERVICE_FEE',
      valueType: 'PERCENTAGE',
      taxValue: 1.5,
      isActive: true,
      effectiveAt: '2026-01-01',
      expiredAt: '',
    },
  ];

  addTax(): void {
    const nextId = Math.max(0, ...this.taxes.map((tax) => tax.id)) + 1;
    this.taxes = [
      ...this.taxes,
      {
        id: nextId,
        taxName: 'New Tax',
        taxType: 'PROCESSING_FEE',
        valueType: 'FIXED',
        taxValue: 0,
        isActive: true,
        effectiveAt: new Date().toISOString().slice(0, 10),
        expiredAt: '',
      },
    ];
  }

  removeTax(taxId: number): void {
    this.taxes = this.taxes.filter((tax) => tax.id !== taxId);
  }

  setValueType(tax: EditableTax, valueType: TaxValueType): void {
    tax.valueType = valueType;
  }

  hasDuplicateActiveTaxType(tax: EditableTax): boolean {
    if (!tax.isActive) {
      return false;
    }

    return this.taxes.some((item) => item.id !== tax.id && item.isActive && item.taxType === tax.taxType);
  }

  get activeTaxCount(): number {
    return this.taxes.filter((tax) => tax.isActive).length;
  }
}
