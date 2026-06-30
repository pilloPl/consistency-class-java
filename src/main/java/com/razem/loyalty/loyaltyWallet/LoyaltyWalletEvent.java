package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.time.Instant;

public sealed interface LoyaltyWalletEvent permits
        LoyaltyWalletEvent.LoyaltyWalletOpened,
        LoyaltyWalletEvent.LoyaltyPointsEarned,
        LoyaltyWalletEvent.LoyaltyPointsRedeemed,
        LoyaltyWalletEvent.RedemptionWindowReset,
        LoyaltyWalletEvent.RedemptionCadenceSet,
        LoyaltyWalletEvent.WalletAccessGranted,
        LoyaltyWalletEvent.WalletAccessRevoked,
        LoyaltyWalletEvent.WalletDeactivated,
        LoyaltyWalletEvent.WalletClosed {

    record LoyaltyWalletOpened(
            WalletNumber walletNumber,
            MemberId ownerId
    ) implements LoyaltyWalletEvent {}

    record LoyaltyPointsEarned(
            WalletNumber walletNumber,
            MemberId ownerId,
            LoyaltyPoints points,
            Instant at
    ) implements LoyaltyWalletEvent {}

    record LoyaltyPointsRedeemed(
            WalletNumber walletNumber,
            MemberId ownerId,
            MemberId byMemberId,
            LoyaltyPoints points,
            LoyaltyPoints burned,
            Instant at
    ) implements LoyaltyWalletEvent {}

    record RedemptionWindowReset(
            WalletNumber walletNumber,
            MemberId ownerId,
            Instant at
    ) implements LoyaltyWalletEvent {}

    record RedemptionCadenceSet(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence
    ) implements LoyaltyWalletEvent {}

    record WalletAccessGranted(
            WalletNumber walletNumber,
            MemberId ownerId,
            MemberId memberId
    ) implements LoyaltyWalletEvent {}

    record WalletAccessRevoked(
            WalletNumber walletNumber,
            MemberId ownerId,
            MemberId memberId
    ) implements LoyaltyWalletEvent {}

    record WalletDeactivated(
            WalletNumber walletNumber,
            MemberId ownerId
    ) implements LoyaltyWalletEvent {}

    record WalletClosed(
            WalletNumber walletNumber
    ) implements LoyaltyWalletEvent {}
}
