export const MIN_TRANSACTION_AMOUNT = 10_000;
export const MAX_TOPUP_AMOUNT = 10_000_000;
export const MAX_PAYMENT_AMOUNT = 10_000_000;
const MAX_MANUAL_AMOUNT_DIGITS = 12;

export interface ParsedTransactionAmount {
  amount: number;
  displayValue: string;
  exceedsLimit: boolean;
}

export function parseTransactionAmount(value: string, maxAmount: number): ParsedTransactionAmount {
  const digits = value
    .replace(/\D/g, '')
    .replace(/^0+(?=\d)/, '')
    .slice(0, MAX_MANUAL_AMOUNT_DIGITS);

  if (!digits) {
    return {
      amount: 0,
      displayValue: '',
      exceedsLimit: false,
    };
  }

  const amount = Number(digits);

  return {
    amount,
    displayValue: formatDigitString(digits),
    exceedsLimit: amount > maxAmount,
  };
}

function formatDigitString(digits: string): string {
  return digits.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}
