package com.razem.loyalty.loyaltyWallet;

public record RedemptionLimit(int value) {
    public static final RedemptionLimit ZERO = new RedemptionLimit(0);

    public static RedemptionLimit of(int value) {
        return new RedemptionLimit(value);
    }
}
