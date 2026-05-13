import { ChangeDetectionStrategy, ChangeDetectorRef, Component, Inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AdminMerchantDto, AdminMerchantTaxDto } from '../../../../core/models/admin-merchant.model';
import { AdminMerchantApiService } from '../../../../core/services/admin-merchant-api.service';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';

interface SuccessNotification {
  title: string;
}

@Component({
  selector: 'app-merchant-management',
  standalone: true,
  imports: [FormsModule, RouterLink, TopAppBarComponent],
  templateUrl: './merchant-management.component.html',
  styleUrl: './merchant-management.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MerchantManagementComponent implements OnInit, OnDestroy {
  private successToastTimer: ReturnType<typeof setTimeout> | null = null;
  readonly successToastDurationMs = 4500;

  searchTerm = '';
  isLoading = false;
  errorMessage = '';
  successNotification: SuccessNotification | null = null;
  merchants: AdminMerchantDto[] = [];

  constructor(
    private adminMerchantApi: AdminMerchantApiService,
    private cdr: ChangeDetectorRef,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: object
  ) {}

  ngOnInit(): void {
    this.successNotification = this.resolveSuccessNotification();
    this.scheduleSuccessToastDismiss();
    this.loadMerchants();
  }

  ngOnDestroy(): void {
    this.clearSuccessToastTimer();
  }

  loadMerchants(): void {
    this.isLoading = true;
    this.errorMessage = '';

    this.adminMerchantApi.getAllMerchants().subscribe({
      next: (response) => {
        this.merchants = response.data;
        this.isLoading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.errorMessage = 'Gagal mengambil data merchant admin.';
        this.isLoading = false;
        this.cdr.markForCheck();
      },
    });
  }

  get filteredMerchants(): AdminMerchantDto[] {
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

  formatTaxSummary(merchant: AdminMerchantDto): string {
    if (merchant.taxes.length === 0) {
      return 'Belum ada tax aktif';
    }

    return merchant.taxes
      .map((tax) => `${this.formatTaxType(tax.taxType)} ${this.formatTaxValue(tax)}`)
      .join(' + ');
  }

  formatTaxType(type: AdminMerchantTaxDto['taxType']): string {
    const labels: Record<AdminMerchantTaxDto['taxType'], string> = {
      PPN: 'PPN',
      SERVICE_FEE: 'Service',
      PLATFORM_FEE: 'Platform',
      ADMIN_FEE: 'Admin',
      TRANSFER_FEE: 'Transfer',
      PROCESSING_FEE: 'Processing',
    };

    return labels[type];
  }

  private formatTaxValue(tax: AdminMerchantTaxDto): string {
    if (tax.valueType === 'PERCENTAGE') {
      return `${tax.taxValue}%`;
    }

    return `Rp ${new Intl.NumberFormat('id-ID').format(tax.taxValue)}`;
  }

  closeSuccessNotification(): void {
    this.clearSuccessToastTimer();
    this.successNotification = null;
    this.cdr.markForCheck();
  }

  private scheduleSuccessToastDismiss(): void {
    if (!this.successNotification || !isPlatformBrowser(this.platformId)) {
      return;
    }

    this.clearSuccessToastTimer();
    this.successToastTimer = setTimeout(() => {
      this.successNotification = null;
      this.successToastTimer = null;
      this.cdr.markForCheck();
    }, this.successToastDurationMs);
  }

  private clearSuccessToastTimer(): void {
    if (this.successToastTimer) {
      clearTimeout(this.successToastTimer);
      this.successToastTimer = null;
    }
  }

  private resolveSuccessNotification(): SuccessNotification | null {
    const state = this.router.getCurrentNavigation()?.extras.state ?? this.getBrowserHistoryState();
    const notification = state?.['successNotification'];

    if (!this.isSuccessNotification(notification)) {
      return null;
    }

    this.clearBrowserHistoryState();
    return notification;
  }

  private getBrowserHistoryState(): Record<string, unknown> | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    return window.history.state as Record<string, unknown>;
  }

  private clearBrowserHistoryState(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }

    window.history.replaceState({}, document.title, window.location.href);
  }

  private isSuccessNotification(value: unknown): value is SuccessNotification {
    if (typeof value !== 'object' || value === null) {
      return false;
    }

    const notification = value as Record<string, unknown>;
    return typeof notification['title'] === 'string';
  }
}
