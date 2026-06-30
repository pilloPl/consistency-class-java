package com.razem.loyalty.loyaltyWallet.activityReport;

import java.time.Instant;

public record ActivityEntry(Kind kind, int points, Instant at) {
    public enum Kind {
        EARNED,
        REDEEMED
    }
}
