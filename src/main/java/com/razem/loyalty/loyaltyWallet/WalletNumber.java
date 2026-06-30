package com.razem.loyalty.loyaltyWallet;

import java.util.UUID;

public record WalletNumber(String value) {
    public static WalletNumber of(String value) {
        return new WalletNumber(value);
    }

    public static WalletNumber random() {
        return new WalletNumber(UUID.randomUUID().toString());
    }
}
