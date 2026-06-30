package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

public sealed interface LoyaltyWallet permits
        LoyaltyWallet.NotExisting,
        LoyaltyWallet.Active,
        LoyaltyWallet.Deactivated,
        LoyaltyWallet.Closed {

    record NotExisting() implements LoyaltyWallet {}

    record Active(
            WalletNumber walletNumber,
            MemberId ownerId,
            LoyaltyPointsLimit pointsLimit,
            RedemptionCadence cadence,
            WalletAccess access
    ) implements LoyaltyWallet {}

    record Deactivated(
            WalletNumber walletNumber,
            MemberId ownerId,
            LoyaltyPointsLimit pointsLimit,
            RedemptionCadence cadence,
            WalletAccess access
    ) implements LoyaltyWallet {}

    record Closed(WalletNumber walletNumber) implements LoyaltyWallet {}

    static NotExisting initial() {
        return new NotExisting();
    }

    static Active open(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount
    ) {
        return new Active(
                walletNumber,
                ownerId,
                LoyaltyPointsLimit.initial(maxRedemptionCount),
                cadence,
                WalletAccess.of(ownerId)
        );
    }

    static Active open(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount,
            LoyaltyPoints earnedPoints,
            LoyaltyPoints redeemedPoints
    ) {
        LoyaltyPointsLimit pointsLimit = LoyaltyPointsLimit.of(
                earnedPoints, redeemedPoints, RedemptionLimit.ZERO, maxRedemptionCount
        );
        return new Active(
                walletNumber,
                ownerId,
                pointsLimit,
                cadence,
                WalletAccess.of(ownerId)
        );
    }
}
