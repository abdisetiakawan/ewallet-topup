package com.berijalan.ewallet.common;

public final class TransactionAmountLimits {

    public static final long MIN_TRANSACTION_AMOUNT = 10_000L;
    public static final long MAX_TOPUP_AMOUNT = 10_000_000L;
    public static final long MAX_PAYMENT_AMOUNT = 10_000_000L;

    private TransactionAmountLimits() {
    }
}
