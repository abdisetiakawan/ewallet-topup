import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { AmountSelectorComponent } from '../../components/amount-selector/amount-selector.component';
import { PaymentSummaryComponent } from '../../components/payment-summary/payment-summary.component';
import { EWallet } from '../../../../core/models/ewallet.model';

@Component({
  selector: 'app-detail-pembayaran',
  standalone: true,
  imports: [
    CommonModule,
    TopAppBarComponent,
    FooterComponent,
    AmountSelectorComponent,
    PaymentSummaryComponent,
  ],
  templateUrl: './detail-pembayaran.component.html',
  styleUrl: './detail-pembayaran.component.css',
})
export class DetailPembayaranComponent implements OnInit {
  readonly accountBalance = 1500000;

  readonly walletData: Record<string, EWallet & { recipient: string; phone: string }> = {
    gopay: { id: 'gopay', name: 'GoPay', icon: 'account_balance_wallet', iconBgColor: '#e5eeff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Bebas biaya admin', recipient: 'Budi Santoso', phone: '+62 812-3456-7890' },
    ovo: { id: 'ovo', name: 'OVO', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA', adminFee: 0, feeLabel: 'Instan', recipient: 'Siti Rahayu', phone: '+62 821-9876-5432' },
    dana: { id: 'dana', name: 'DANA', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Instan', recipient: 'Ahmad Fauzi', phone: '+62 831-1234-5678' },
    shopeepay: { id: 'shopeepay', name: 'ShopeePay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D', adminFee: 500, feeLabel: 'Biaya Rp 500', recipient: 'Dewi Lestari', phone: '+62 851-5678-9012' },
    linkaja: { id: 'linkaja', name: 'LinkAja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a', adminFee: 0, feeLabel: 'Instan', recipient: 'Rudi Hermawan', phone: '+62 813-3456-7890' },
  };

  selectedWallet!: (EWallet & { recipient: string; phone: string });
  selectedAmount: number = 0;

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    const walletId = this.route.snapshot.paramMap.get('walletId') ?? 'gopay';
    this.selectedWallet = this.walletData[walletId] ?? this.walletData['gopay'];
  }

  get adminFee(): number {
    return this.selectedWallet?.adminFee ?? 1000;
  }

  onAmountChange(amount: number): void {
    this.selectedAmount = amount;
  }

  onPay(): void {
    alert(`Pembayaran berhasil!\n${this.selectedWallet.name} - ${this.selectedWallet.recipient}\nTotal: Rp ${new Intl.NumberFormat('id-ID').format(this.selectedAmount + this.adminFee)}`);
  }

  goBack(): void {
    this.router.navigate(['/topup']);
  }

  format(value: number): string {
    return new Intl.NumberFormat('id-ID').format(value);
  }
}
