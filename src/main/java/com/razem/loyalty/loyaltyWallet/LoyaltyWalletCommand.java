package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.time.Instant;

public sealed interface LoyaltyWalletCommand permits
        LoyaltyWalletCommand.OpenLoyaltyWallet,
        LoyaltyWalletCommand.EarnLoyaltyPoints,
        LoyaltyWalletCommand.RedeemLoyaltyPoints,
        LoyaltyWalletCommand.SetRedemptionCadence,
        LoyaltyWalletCommand.GrantWalletAccess,
        LoyaltyWalletCommand.RevokeWalletAccess,
        LoyaltyWalletCommand.ResetRedemptionWindow,
        LoyaltyWalletCommand.DeactivateWallet,
        LoyaltyWalletCommand.CloseWallet {

    record OpenLoyaltyWallet(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount,
            LoyaltyPoints earnedPoints,
            LoyaltyPoints redeemedPoints
    ) implements LoyaltyWalletCommand {
        public OpenLoyaltyWallet(WalletNumber walletNumber, MemberId ownerId, RedemptionCadence cadence, RedemptionLimit maxRedemptionCount) {
            this(walletNumber, ownerId, cadence, maxRedemptionCount, LoyaltyPoints.ZERO, LoyaltyPoints.ZERO);
        }
    }

    record EarnLoyaltyPoints(
            WalletNumber walletNumber,
            LoyaltyPoints points,
            Instant at
    ) implements LoyaltyWalletCommand {}

    record RedeemLoyaltyPoints(
            WalletNumber walletNumber,
            MemberId memberId,
            LoyaltyPoints points,
            LoyaltyPoints burned,
            Instant at
    ) implements LoyaltyWalletCommand {
        public RedeemLoyaltyPoints(WalletNumber walletNumber, MemberId memberId, LoyaltyPoints points, Instant at) {
            this(walletNumber, memberId, points, points, at);
        }
    }

    record SetRedemptionCadence(
            WalletNumber walletNumber,
            RedemptionCadence cadence
    ) implements LoyaltyWalletCommand {}

    record GrantWalletAccess(
            WalletNumber walletNumber,
            MemberId memberId
    ) implements LoyaltyWalletCommand {}

    record RevokeWalletAccess(
            WalletNumber walletNumber,
            MemberId memberId
    ) implements LoyaltyWalletCommand {}

    record ResetRedemptionWindow(
            WalletNumber walletNumber,
            Instant at
    ) implements LoyaltyWalletCommand {}

    record DeactivateWallet(
            WalletNumber walletNumber
    ) implements LoyaltyWalletCommand {}

    record CloseWallet(
            WalletNumber walletNumber
    ) implements LoyaltyWalletCommand {}
}
