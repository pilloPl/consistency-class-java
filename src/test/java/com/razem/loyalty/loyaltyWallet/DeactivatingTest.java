package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class DeactivatingTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void deactivatesActiveWalletKeepingDataAndEmitsEvent() {
        LoyaltyWallet wallet = walletWithPoints(100);
        wallet.redeemPoints(owner, LoyaltyPoints.of(10), at);

        List<LoyaltyWalletEvent> events = wallet.deactivate();

        assertThat(wallet.isDeactivated()).isTrue();
        assertThat(wallet.walletNumber()).isEqualTo(walletNumber);
        assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(90);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new WalletDeactivated(walletNumber, owner));
    }

    @Test
    void leavesAlreadyDeactivatedWalletUnchangedWithoutEvents() {
        LoyaltyWallet wallet = deactivated();

        List<LoyaltyWalletEvent> events = wallet.deactivate();

        assertThat(events).isEmpty();
        assertThat(wallet.isDeactivated()).isTrue();
    }

    @Test
    void cannotDeactivateNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWallet.notExisting().deactivate()
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    @Test
    void cannotDeactivateClosedWallet() {
        assertThatThrownBy(() ->
                closed().deactivate()
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
