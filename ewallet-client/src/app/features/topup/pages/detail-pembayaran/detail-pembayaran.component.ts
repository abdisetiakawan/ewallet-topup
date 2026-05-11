import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AmountSelectorComponent } from '../../components/amount-selector/amount-selector.component';
import { PaymentSummaryComponent } from '../../components/payment-summary/payment-summary.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';
import { AuthService } from '../../../../core/services/auth.service';
import { UserSummary } from '../../../../core/models/auth.model';

type WalletPaymentTarget = EWallet & {
  merchantName: string;
};

@Component({
  selector: 'app-detail-pembayaran',
  standalone: true,
  imports: [
    CommonModule,
    AmountSelectorComponent,
    PaymentSummaryComponent,
  ],
  templateUrl: './detail-pembayaran.component.html',
  styleUrl: './detail-pembayaran.component.css',
})
export class DetailPembayaranComponent implements OnInit {
  private readonly destroyRef = inject(DestroyRef);

  accountBalance = 0;
  isSubmitting = false;
  errorMessage: string | null = null;
  currentUser: UserSummary | null = null;

  readonly walletData: Record<string, WalletPaymentTarget> = {
    gopay: { id: 'gopay', name: 'GoPay', icon: 'account_balance_wallet', iconBgColor: '#e5eeff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Bebas biaya admin', merchantName: 'Gopay' },
    ovo: { id: 'ovo', name: 'OVO', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA', adminFee: 0, feeLabel: 'Instan', merchantName: 'ovo' },
    dana: { id: 'dana', name: 'DANA', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Instan', merchantName: 'Dana' },
    shopeepay: { id: 'shopeepay', name: 'ShopeePay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D', adminFee: 500, feeLabel: 'Biaya Rp 500', merchantName: 'Shoopepay' },
    linkaja: { id: 'linkaja', name: 'LinkAja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a', adminFee: 0, feeLabel: 'Instan', merchantName: 'linkAja' },
  };

  selectedWallet: WalletPaymentTarget = this.walletData['gopay'];
  selectedAmount: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletStore: WalletStoreService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const walletId = this.route.snapshot.paramMap.get('walletId') ?? 'gopay';
    this.selectedWallet = this.walletData[walletId] ?? this.walletData['gopay'];
    this.currentUser = this.authService.getCurrentUser();

    this.walletStore.balance$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((balance) => {
        this.accountBalance = balance;
      });

    this.walletStore.loadBalance().subscribe({
      error: () => {
        this.errorMessage = 'Gagal memuat saldo terbaru.';
      },
    });
  }

  get adminFee(): number {
    return this.selectedWallet?.adminFee ?? 1000;
  }

  get recipientName(): string {
    return this.currentUser?.name || 'Akun WalletPay';
  }

  get recipientContact(): string {
    return this.currentUser?.email || 'Email akun tidak tersedia';
  }

  onAmountChange(amount: number): void {
    this.selectedAmount = amount;
  }

  onPay(): void {
    if (this.isSubmitting || this.selectedAmount < 10000) {
      return;
    }

    this.isSubmitting = true;
    this.errorMessage = null;

    const total = this.selectedAmount + this.adminFee;
    this.walletStore.pay({
      merchantName: this.selectedWallet.merchantName,
      amount: total,
      description: `Top-up ${this.selectedWallet.name} untuk ${this.recipientName}`,
    }).subscribe({
      next: () => {
        this.router.navigate(['/topup'], {
          state: {
            successNotification: {
              icon: 'check_circle',
              title: 'Payment sukses',
              message: '',
              amountLabel: `- Rp ${this.format(total)}`,
            },
          },
        });
      },
      error: () => {
        this.isSubmitting = false;
        this.errorMessage = 'Pembayaran gagal diproses. Pastikan saldo cukup dan coba lagi.';
      },
      complete: () => {
        this.isSubmitting = false;
      },
    });
  }

  goBack(): void {
    this.router.navigate(['/topup']);
  }

  format(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }
}
