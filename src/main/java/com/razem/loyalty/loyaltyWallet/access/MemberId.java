package com.razem.loyalty.loyaltyWallet.access;

import java.util.UUID;

public record MemberId(String value) {
    public static MemberId of(String value) {
        return new MemberId(value);
    }

    public static MemberId random() {
        return new MemberId(UUID.randomUUID().toString());
    }
}
