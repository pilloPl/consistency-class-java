package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class EarningPointsTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void earnsPointsOnActiveWalletAndEmitsEvent() {
        LoyaltyWallet wallet = openWallet();

        List<LoyaltyWalletEvent> events = wallet.earnPoints(LoyaltyPoints.of(100), at);

        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(100);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(
                new LoyaltyPointsEarned(walletNumber, owner, LoyaltyPoints.of(100), at)
        );
    }

    @Test
    void cannotEarnOnNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWallet.notExisting().earnPoints(LoyaltyPoints.of(100), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    @Test
    void cannotEarnOnDeactivatedWallet() {
        assertThatThrownBy(() ->
                deactivated().earnPoints(LoyaltyPoints.of(100), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
    }

    @Test
    void cannotEarnOnClosedWallet() {
        assertThatThrownBy(() ->
                closed().earnPoints(LoyaltyPoints.of(100), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
    }

    private LoyaltyWallet openWallet() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
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
