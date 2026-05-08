export interface EWallet {
  id: string;
  name: string;
  icon: string;
  iconBgColor: string;
  iconTextColor: string;
  adminFee: number;
  feeLabel: string;
  featured?: boolean;
}
