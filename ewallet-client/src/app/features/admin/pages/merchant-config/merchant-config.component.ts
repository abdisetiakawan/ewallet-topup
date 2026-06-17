import { ChangeDetectionStrategy, ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../../core/services/auth.service';
import {
  AdminMerchantConfigPayload,
  AdminMerchantDto,
  AdminTaxType,
  AdminTaxValueType,
} from '../../../../core/models/admin-merchant.model';
import { AdminMerchantApiService } from '../../../../core/services/admin-merchant-api.service';

interface EditableTax {
  id: number | null;
  taxName: string;
  taxType: AdminTaxType;
  valueType: AdminTaxValueType;
  taxValue: number;
  isActive: boolean;
  effectiveAt: string;
  expiredAt: string;
}

@Component({
  selector: 'app-merchant-config',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './merchant-config.component.html',
  styleUrl: './merchant-config.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MerchantConfigComponent implements OnInit {
  merchantId: number | null = null;
  merchantName = '';
  isMerchantActive = true;
  isLoading = false;
  isSaving = false;
  errorMessage = '';
  successMessage = '';

  readonly taxTypeOptions: { value: AdminTaxType; label: string }[] = [
    { value: 'PPN', label: 'PPN' },
    { value: 'ADMIN_FEE', label: 'Admin Fee' },
    { value: 'SERVICE_FEE', label: 'Service Fee' },
    { value: 'PLATFORM_FEE', label: 'Platform Fee' },
    { value: 'TRANSFER_FEE', label: 'Transfer Fee' },
    { value: 'PROCESSING_FEE', label: 'Processing Fee' },
  ];

  taxes: EditableTax[] = [];

  private authService = inject(AuthService);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private adminMerchantApi: AdminMerchantApiService,
    private cdr: ChangeDetectorRef
  ) {}

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login']),
    });
  }

  ngOnInit(): void {
    const merchantIdParam = this.route.snapshot.paramMap.get('merchantId');

    if (merchantIdParam === 'new') {
      this.addTax();
      return;
    }

    const parsedMerchantId = Number(merchantIdParam);
    if (!Number.isFinite(parsedMerchantId)) {
      this.errorMessage = 'Merchant tidak valid.';
      return;
    }

    this.merchantId = parsedMerchantId;
    this.loadMerchant(parsedMerchantId);
  }

  loadMerchant(id: number): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminMerchantApi.getMerchant(id).subscribe({
      next: (response) => {
        this.applyMerchant(response.data);
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Gagal mengambil konfigurasi merchant.';
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  addTax(): void {
    this.taxes = [
      ...this.taxes,
      {
        id: null,
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

  removeTaxAt(index: number): void {
    this.taxes = this.taxes.filter((_, itemIndex) => itemIndex !== index);
  }

  setValueType(tax: EditableTax, valueType: AdminTaxValueType): void {
    tax.valueType = valueType;
  }

  hasDuplicateActiveTaxType(tax: EditableTax): boolean {
    if (!tax.isActive) {
      return false;
    }

    return this.taxes.some((item) => item !== tax && item.isActive && item.taxType === tax.taxType);
  }

  get activeTaxCount(): number {
    return this.taxes.filter((tax) => tax.isActive).length;
  }

  save(): void {
    if (!this.merchantName.trim()) {
      this.errorMessage = 'Merchant name wajib diisi.';
      return;
    }

    if (this.taxes.some((tax) => this.hasDuplicateActiveTaxType(tax))) {
      this.errorMessage = 'Tax type aktif tidak boleh duplikat.';
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.successMessage = '';

    const request = this.merchantId
      ? this.adminMerchantApi.updateMerchant(this.merchantId, this.toPayload())
      : this.adminMerchantApi.createMerchant(this.toPayload());

    request.subscribe({
      next: (response) => {
        this.applyMerchant(response.data);
        this.isSaving = false;
        this.router.navigate(['/admin/merchants'], {
          state: {
            successNotification: {
              title: 'Konfigurasi merchant berhasil disimpan',
            },
          },
        });

        this.cdr.markForCheck();
      },
      error: (error) => {
        this.errorMessage = error?.error?.message || 'Gagal menyimpan konfigurasi merchant.';
        this.isSaving = false;
        this.cdr.markForCheck();
      },
    });
  }

  private applyMerchant(merchant: AdminMerchantDto): void {
    this.merchantId = merchant.id;
    this.merchantName = merchant.name;
    this.isMerchantActive = merchant.isActive;
    this.taxes = merchant.taxes.map((tax) => ({
      id: tax.id,
      taxName: tax.taxName,
      taxType: tax.taxType,
      valueType: tax.valueType,
      taxValue: tax.taxValue,
      isActive: tax.isActive,
      effectiveAt: this.toDateInput(tax.effectiveAt),
      expiredAt: this.toDateInput(tax.expiredAt),
    }));
  }

  private toPayload(): AdminMerchantConfigPayload {
    return {
      name: this.merchantName.trim(),
      isActive: this.isMerchantActive,
      taxes: this.taxes.map((tax) => ({
        id: tax.id,
        taxName: tax.taxName.trim(),
        taxType: tax.taxType,
        valueType: tax.valueType,
        taxValue: Number(tax.taxValue),
        isActive: tax.isActive,
        effectiveAt: `${tax.effectiveAt}T00:00:00`,
        expiredAt: tax.expiredAt ? `${tax.expiredAt}T00:00:00` : null,
      })),
    };
  }

  private toDateInput(value: string | null): string {
    return value ? value.slice(0, 10) : '';
  }
}
