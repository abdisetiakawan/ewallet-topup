import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TopAppBarComponent } from '../../../../shared/components/top-app-bar/top-app-bar.component';
import { BottomNavBarComponent } from '../../../../shared/components/bottom-nav-bar/bottom-nav-bar.component';
import { FooterComponent } from '../../../../shared/components/footer/footer.component';
import { BalanceCardComponent } from '../../components/balance-card/balance-card.component';
import { EwalletCardComponent } from '../../components/ewallet-card/ewallet-card.component';
import { EWallet } from '../../../../core/models/ewallet.model';
import { TOPUP_ACCOUNT_BALANCE } from '../../constants/topup.constants';

@Component({
  selector: 'app-pilih-ewallet',
  standalone: true,
  imports: [
    CommonModule,
    TopAppBarComponent,
    BottomNavBarComponent,
    FooterComponent,
    BalanceCardComponent,
    EwalletCardComponent,
  ],
  templateUrl: './pilih-ewallet.component.html',
  styleUrl: './pilih-ewallet.component.css',
})
export class PilihEwalletComponent {
  readonly balance = TOPUP_ACCOUNT_BALANCE;

  readonly ewallets: EWallet[] = [
    { id: 'gopay', name: 'GoPay', icon: 'payments', iconBgColor: '#e5eeff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Bebas biaya admin', featured: true },
    { id: 'ovo', name: 'OVO', icon: 'toll', iconBgColor: '#E5E0F4', iconTextColor: '#4A25AA', adminFee: 0, feeLabel: 'Instan' },
    { id: 'dana', name: 'DANA', icon: 'account_balance_wallet', iconBgColor: '#dce9ff', iconTextColor: '#0058be', adminFee: 0, feeLabel: 'Instan' },
    { id: 'shopeepay', name: 'ShopeePay', icon: 'local_mall', iconBgColor: '#FCE3D9', iconTextColor: '#EE4D2D', adminFee: 500, feeLabel: 'Biaya Rp 500' },
    { id: 'linkaja', name: 'LinkAja', icon: 'link', iconBgColor: '#ffdad6', iconTextColor: '#93000a', adminFee: 0, feeLabel: 'Instan' },
  ];

  constructor(private router: Router) {}

  onWalletSelected(wallet: EWallet): void {
    this.router.navigate(['/topup/detail', wallet.id]);
  }
}
