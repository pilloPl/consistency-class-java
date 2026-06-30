package com.razem.loyalty.loyaltyWallet;

public record LoyaltyPointsLimit(
        LoyaltyPoints earnedPoints,
        LoyaltyPoints redeemedPoints,
        LoyaltyPoints availablePoints,
        RedemptionLimit redemptionCount,
        RedemptionLimit maxRedemptionCount
) {
    public static LoyaltyPointsLimit initial(RedemptionLimit maxRedemptionCount) {
        return new LoyaltyPointsLimit(
                LoyaltyPoints.ZERO,
                LoyaltyPoints.ZERO,
                LoyaltyPoints.ZERO,
                RedemptionLimit.ZERO,
                maxRedemptionCount
        );
    }

    public static LoyaltyPointsLimit of(
            LoyaltyPoints earnedPoints,
            LoyaltyPoints redeemedPoints,
            RedemptionLimit redemptionCount,
            RedemptionLimit maxRedemptionCount
    ) {
        LoyaltyPoints available = LoyaltyPoints.of(earnedPoints.value() - redeemedPoints.value());
        return new LoyaltyPointsLimit(earnedPoints, redeemedPoints, available, redemptionCount, maxRedemptionCount);
    }

    public LoyaltyPointsLimit earn(LoyaltyPoints points) {
        return new LoyaltyPointsLimit(
                new LoyaltyPoints(earnedPoints.value() + points.value()),
                redeemedPoints,
                new LoyaltyPoints(availablePoints.value() + points.value()),
                redemptionCount,
                maxRedemptionCount
        );
    }

    public LoyaltyPointsLimit redeem(LoyaltyPoints requested, LoyaltyPoints burned) {
        return new LoyaltyPointsLimit(
                earnedPoints,
                new LoyaltyPoints(redeemedPoints.value() + requested.value()),
                new LoyaltyPoints(availablePoints.value() - burned.value()),
                new RedemptionLimit(redemptionCount.value() + 1),
                maxRedemptionCount
        );
    }

    public LoyaltyPointsLimit resetRedemptionCount() {
        return new LoyaltyPointsLimit(
                earnedPoints,
                redeemedPoints,
                availablePoints,
                RedemptionLimit.ZERO,
                maxRedemptionCount
        );
    }

    public int redemptionsLeft() {
        return maxRedemptionCount.value() - redemptionCount.value();
    }

    public boolean canRedeem(LoyaltyPoints burned) {
        return availablePoints.value() >= burned.value() && redemptionsLeft() > 0;
    }
}
