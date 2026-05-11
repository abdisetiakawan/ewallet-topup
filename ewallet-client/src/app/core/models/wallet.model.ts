export interface WalletBalanceResponse {
  balance: number;
  updatedAt: string;
}

export interface TopupRequest {
  amount: number;
}

export interface TopupResponse {
  transactionId: number;
  amount: number;
  balanceBefore: number;
  balanceAfter: number;
  type: string;
  status: string;
  createdAt: string;
}
