package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class OpeningTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void opensNotExistingWalletAndEmitsEvent() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();

        List<LoyaltyWalletEvent> events = wallet.open(
                walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
        );

        assertThat(wallet.isActive()).isTrue();
        assertThat(wallet.walletNumber()).isEqualTo(walletNumber);
        assertThat(wallet.ownerId()).isEqualTo(owner);
        assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(0);
        assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(5);
        assertThat(wallet.access().hasAccess(owner)).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new LoyaltyWalletOpened(walletNumber, owner));
    }

    @Test
    void leavesAlreadyActiveWalletUnchangedWithoutEvents() {
        LoyaltyWallet wallet = openWallet();

        List<LoyaltyWalletEvent> events = wallet.open(
                walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)
        );

        assertThat(events).isEmpty();
        assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
    }

    @Test
    void leavesDeactivatedWalletUnchangedWithoutEvents() {
        LoyaltyWallet wallet = deactivated();

        List<LoyaltyWalletEvent> events = wallet.open(
                walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
        );

        assertThat(events).isEmpty();
        assertThat(wallet.isDeactivated()).isTrue();
    }

    @Test
    void leavesClosedWalletUnchangedWithoutEvents() {
        LoyaltyWallet wallet = closed();

        List<LoyaltyWalletEvent> events = wallet.open(
                walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
        );

        assertThat(events).isEmpty();
        assertThat(wallet.isClosed()).isTrue();
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
