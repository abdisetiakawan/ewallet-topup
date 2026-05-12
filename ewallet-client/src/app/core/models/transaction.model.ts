export interface PaymentRequest {
  merchantName: string;
  amount: number;
  description: string;
}

export interface PaymentResponse {
  transactionId: number;
  referenceId: string;
  amount: number;
  baseAmount: number;
  taxAmount: number;
  balanceBefore: number;
  balanceAfter: number;
  description: string;
  merchantName: string;
  type: string;
  status: string;
}
