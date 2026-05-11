import { Component, DestroyRef, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AmountSelectorComponent } from '../../components/amount-selector/amount-selector.component';
import { PaymentSummaryComponent } from '../../components/payment-summary/payment-summary.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { WalletStoreService } from '../../../../core/services/wallet-store.service';

type WalletPaymentTarget = EWallet & {
  recipient: string;
  phone: string;
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

  readonly walletData: Record<string, WalletPaymentTarget> = {
    gopay: { id: 'gopay', name: 'GoPay', icon: 'account_balance_wallet', iconBgColor: '#e5eeff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Bebas biaya admin', recipient: 'Budi Santoso', phone: '+62 812-3456-7890', merchantName: 'Gopay' },
    ovo: { id: 'ovo', name: 'OVO', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA', adminFee: 0, feeLabel: 'Instan', recipient: 'Siti Rahayu', phone: '+62 821-9876-5432', merchantName: 'ovo' },
    dana: { id: 'dana', name: 'DANA', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Instan', recipient: 'Ahmad Fauzi', phone: '+62 831-1234-5678', merchantName: 'Dana' },
    shopeepay: { id: 'shopeepay', name: 'ShopeePay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D', adminFee: 500, feeLabel: 'Biaya Rp 500', recipient: 'Dewi Lestari', phone: '+62 851-5678-9012', merchantName: 'Shoopepay' },
    linkaja: { id: 'linkaja', name: 'LinkAja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a', adminFee: 0, feeLabel: 'Instan', recipient: 'Rudi Hermawan', phone: '+62 813-3456-7890', merchantName: 'linkAja' },
  };

  selectedWallet: WalletPaymentTarget = this.walletData['gopay'];
  selectedAmount: number = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private walletStore: WalletStoreService
  ) {}

  ngOnInit(): void {
    const walletId = this.route.snapshot.paramMap.get('walletId') ?? 'gopay';
    this.selectedWallet = this.walletData[walletId] ?? this.walletData['gopay'];

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
      description: `Top-up ${this.selectedWallet.name} ${this.selectedWallet.phone}`,
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
