export type TransactionType = 'TOPUP' | 'TRANSFER' | 'PAYMENT';
export type TransactionStatus = 'PENDING' | 'SUCCESS' | 'FAILED';

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
  type: TransactionType;
  status: TransactionStatus;
}

export interface TransactionHistoryQuery {
  page?: number;
  size?: number;
  status?: TransactionStatus;
  type?: TransactionType;
}

export interface TransactionHistoryItem {
  transactionId: number;
  userId: number;
  userName: string;
  userEmail: string;
  referenceId: string;
  amount: number;
  baseAmount: number;
  taxAmount: number;
  balanceBefore: number;
  balanceAfter: number;
  type: TransactionType;
  status: TransactionStatus;
  description: string | null;
  merchantName: string | null;
  createdAt: string;
}

export interface TransactionHistoryResponse {
  content: TransactionHistoryItem[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
