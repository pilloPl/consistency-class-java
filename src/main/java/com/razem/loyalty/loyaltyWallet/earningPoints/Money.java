package com.razem.loyalty.loyaltyWallet.earningPoints;

public record Money(int value) {
    public static Money of(int value) {
        return new Money(value);
    }
}
