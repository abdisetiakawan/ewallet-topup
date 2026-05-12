export interface EWallet {
  id: string;
  name: string;
  icon: string;
  iconBgColor: string;
  iconTextColor: string;
  feeValue: number;
  feeType: 'FIXED' | 'PERCENTAGE';
  feeLabel: string;
  featured?: boolean;
}
