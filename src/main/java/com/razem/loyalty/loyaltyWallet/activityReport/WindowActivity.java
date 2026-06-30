package com.razem.loyalty.loyaltyWallet.activityReport;

import java.util.List;

public record WindowActivity(
        int windowNumber,
        int earned,
        int redeemed,
        int burned,
        int redemptionCount,
        boolean hadActivity,
        List<ActivityEntry> entries
) {
    public static WindowActivity empty(int windowNumber) {
        return new WindowActivity(windowNumber, 0, 0, 0, 0, false, List.of());
    }
}
