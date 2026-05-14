package com.berijalan.ewallet.util;

import java.util.UUID;

public final class ReferenceIdGenerator {
    private ReferenceIdGenerator() {}

    public static String generate(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
