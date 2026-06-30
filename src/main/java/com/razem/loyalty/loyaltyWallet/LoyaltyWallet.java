package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.time.Instant;
import java.util.List;

public class LoyaltyWallet {

    private enum Status { NOT_EXISTING, ACTIVE, DEACTIVATED, CLOSED }

    private WalletNumber walletNumber;
    private MemberId ownerId;
    private LoyaltyPointsLimit pointsLimit;
    private RedemptionCadence cadence;
    private WalletAccess access;
    private Status status = Status.NOT_EXISTING;

    public static LoyaltyWallet notExisting() {
        return new LoyaltyWallet();
    }

    public List<LoyaltyWalletEvent> open(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount
    ) {
        return open(walletNumber, ownerId, cadence, maxRedemptionCount, LoyaltyPoints.ZERO, LoyaltyPoints.ZERO);
    }

    public List<LoyaltyWalletEvent> open(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount,
            LoyaltyPoints earnedPoints,
            LoyaltyPoints redeemedPoints
    ) {
        if (status != Status.NOT_EXISTING) {
            return List.of();
        }
        this.walletNumber = walletNumber;
        this.ownerId = ownerId;
        this.pointsLimit = LoyaltyPointsLimit.of(
                earnedPoints, redeemedPoints, RedemptionLimit.ZERO, maxRedemptionCount
        );
        this.cadence = cadence;
        this.access = WalletAccess.of(ownerId);
        this.status = Status.ACTIVE;
        return List.of(new LoyaltyWalletOpened(this.walletNumber, this.ownerId));
    }

    public List<LoyaltyWalletEvent> earnPoints(LoyaltyPoints points, Instant at) {
        assertActive();
        this.pointsLimit = this.pointsLimit.earn(points);
        return List.of(new LoyaltyPointsEarned(walletNumber, ownerId, points, at));
    }

    public List<LoyaltyWalletEvent> redeemPoints(
            MemberId memberId,
            LoyaltyPoints points,
            LoyaltyPoints burned,
            Instant at
    ) {
        assertActive();
        if (!access.hasAccess(memberId)) {
            throw new IllegalStateException("Not authorized to redeem");
        }
        if (pointsLimit.redemptionsLeft() <= 0) {
            throw new IllegalStateException("Redemption window exhausted");
        }
        if (pointsLimit.availablePoints().value() < burned.value()) {
            throw new IllegalStateException("Not enough points to redeem");
        }
        this.pointsLimit = this.pointsLimit.redeem(points, burned);
        return List.of(new LoyaltyPointsRedeemed(walletNumber, ownerId, memberId, points, burned, at));
    }

    public List<LoyaltyWalletEvent> redeemPoints(MemberId memberId, LoyaltyPoints points, Instant at) {
        return redeemPoints(memberId, points, points, at);
    }

    public List<LoyaltyWalletEvent> setRedemptionCadence(RedemptionCadence cadence) {
        assertActive();
        this.cadence = cadence;
        return List.of(new RedemptionCadenceSet(walletNumber, ownerId, cadence));
    }

    public List<LoyaltyWalletEvent> grantAccess(MemberId memberId) {
        assertActive();
        this.access = this.access.grant(memberId);
        return List.of(new WalletAccessGranted(walletNumber, ownerId, memberId));
    }

    public List<LoyaltyWalletEvent> revokeAccess(MemberId memberId) {
        assertActive();
        this.access = this.access.revoke(memberId);
        return List.of(new WalletAccessRevoked(walletNumber, ownerId, memberId));
    }

    public List<LoyaltyWalletEvent> resetRedemptionWindow(Instant at) {
        assertActive();
        this.pointsLimit = this.pointsLimit.resetRedemptionCount();
        return List.of(new RedemptionWindowReset(walletNumber, ownerId, at));
    }

    public List<LoyaltyWalletEvent> deactivate() {
        if (status == Status.DEACTIVATED) {
            return List.of();
        }
        assertActive();
        this.status = Status.DEACTIVATED;
        return List.of(new WalletDeactivated(walletNumber, ownerId));
    }

    public List<LoyaltyWalletEvent> close() {
        if (status == Status.CLOSED) {
            return List.of();
        }
        if (status == Status.NOT_EXISTING) {
            throw new IllegalStateException("Wallet doesn't exist");
        }
        this.status = Status.CLOSED;
        return List.of(new WalletClosed(walletNumber));
    }

    public WalletNumber walletNumber() {
        return walletNumber;
    }

    public MemberId ownerId() {
        return ownerId;
    }

    public LoyaltyPointsLimit pointsLimit() {
        return pointsLimit;
    }

    public RedemptionCadence cadence() {
        return cadence;
    }

    public WalletAccess access() {
        return access;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }

    public boolean isDeactivated() {
        return status == Status.DEACTIVATED;
    }

    public boolean isClosed() {
        return status == Status.CLOSED;
    }

    public boolean isNotExisting() {
        return status == Status.NOT_EXISTING;
    }

    private void assertActive() {
        switch (status) {
            case NOT_EXISTING -> throw new IllegalStateException("Wallet doesn't exist");
            case DEACTIVATED -> throw new IllegalStateException("Wallet is not active");
            case CLOSED -> throw new IllegalStateException("Wallet is closed");
            case ACTIVE -> {}
        }
    }
}
