package com.berijalan.ewallet.util;

import java.util.UUID;

public final class ReferenceIdGenerator {
    private ReferenceIdGenerator() {}

    /**
     * Membuat reference ID yang aman ditampilkan ke client tanpa mengekspos ID database berurutan.
     *
     * @param prefix prefix domain transaksi, misalnya TXN- atau PAY-.
     * @return reference ID unik berbasis UUID.
     */
    public static String generate(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
