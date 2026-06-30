package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RedeemingPointsTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void redeemsPointsOnActiveWalletAndEmitsEvent() {
        LoyaltyWallet wallet = walletWithPoints(100);

        List<LoyaltyWalletEvent> events = wallet.redeemPoints(owner, LoyaltyPoints.of(40), at);

        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
        assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(4);
        assertThat(events).hasSize(1);
        LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) events.get(0);
        assertThat(event.walletNumber()).isEqualTo(walletNumber);
        assertThat(event.ownerId()).isEqualTo(owner);
        assertThat(event.byMemberId()).isEqualTo(owner);
        assertThat(event.points().value()).isEqualTo(40);
        assertThat(event.burned().value()).isEqualTo(40);
    }

    @Test
    void burnsPolicyAmountFromBalanceWhileRecordingRedeemedAmount() {
        LoyaltyWallet wallet = walletWithPoints(100);

        List<LoyaltyWalletEvent> events = wallet.redeemPoints(
                owner, LoyaltyPoints.of(100), LoyaltyPoints.of(95), at
        );

        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(5);
        LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) events.get(0);
        assertThat(event.points().value()).isEqualTo(100);
        assertThat(event.burned().value()).isEqualTo(95);
    }

    @Test
    void cannotRedeemWhenRedemptionWindowExhausted() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(1));
        wallet.earnPoints(LoyaltyPoints.of(100), at);
        wallet.redeemPoints(owner, LoyaltyPoints.of(10), at);

        assertThatThrownBy(() ->
                wallet.redeemPoints(owner, LoyaltyPoints.of(10), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Redemption window exhausted");
    }

    @Test
    void cannotRedeemMoreThanAvailable() {
        assertThatThrownBy(() ->
                walletWithPoints(20).redeemPoints(owner, LoyaltyPoints.of(50), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not enough points to redeem");
    }

    @Test
    void cannotRedeemOnNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWallet.notExisting().redeemPoints(owner, LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    @Test
    void cannotRedeemOnDeactivatedWallet() {
        assertThatThrownBy(() ->
                deactivated().redeemPoints(owner, LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
    }

    @Test
    void cannotRedeemOnClosedWallet() {
        assertThatThrownBy(() ->
                closed().redeemPoints(owner, LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
    }

    private LoyaltyWallet openWallet() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        return wallet;
    }

    private LoyaltyWallet walletWithPoints(int points) {
        LoyaltyWallet wallet = openWallet();
        wallet.earnPoints(LoyaltyPoints.of(points), at);
        return wallet;
    }

    private LoyaltyWallet deactivated() {
        LoyaltyWallet wallet = openWallet();
        wallet.deactivate();
        return wallet;
    }

    private LoyaltyWallet closed() {
        LoyaltyWallet wallet = openWallet();
        wallet.close();
        return wallet;
    }
}
