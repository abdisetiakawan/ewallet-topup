export interface TopupDetail {
  wallet: string;
  walletName: string;
  recipientName: string;
  phoneNumber: string;
  amount: number;
  adminFee: number;
  total: number;
  paymentMethod: string;
  paymentBalance: number;
}

export interface AmountOption {
  value: number;
  label: string;
}
