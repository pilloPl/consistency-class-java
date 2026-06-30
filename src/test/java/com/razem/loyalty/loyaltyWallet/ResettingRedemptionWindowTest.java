package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ResettingRedemptionWindowTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void resetsRedemptionCountKeepingBalanceAndEmitsEvent() {
        LoyaltyWallet wallet = walletWithPoints(100);
        wallet.redeemPoints(owner, LoyaltyPoints.of(10), at);
        assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(4);

        List<LoyaltyWalletEvent> events = wallet.resetRedemptionWindow(at);

        assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(5);
        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(90);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new RedemptionWindowReset(walletNumber, owner, at));
    }

    @Test
    void cannotResetWindowIfWalletNotActive() {
        assertThatThrownBy(() ->
                deactivated().resetRedemptionWindow(at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
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
}
