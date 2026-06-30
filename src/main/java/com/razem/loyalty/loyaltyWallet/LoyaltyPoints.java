package com.razem.loyalty.loyaltyWallet;

public record LoyaltyPoints(int value) {
    public static final LoyaltyPoints ZERO = new LoyaltyPoints(0);

    public static LoyaltyPoints of(int value) {
        return new LoyaltyPoints(value);
    }
}
