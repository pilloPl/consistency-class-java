package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies that every state-changing operation produces at least one event.
 * Idempotent transitions (e.g. already-deactivated) are allowed to return no events.
 */
class AllCommandsProduceEventsTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final MemberId familyMember = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

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

    @Test
    void openLoyaltyWalletProducesLoyaltyWalletOpenedEvent() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();

        List<LoyaltyWalletEvent> events = wallet.open(
                walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
        );

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(LoyaltyWalletOpened.class);
    }

    @Test
    void earnLoyaltyPointsProducesLoyaltyPointsEarnedEvent() {
        List<LoyaltyWalletEvent> events = openWallet().earnPoints(LoyaltyPoints.of(100), at);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(LoyaltyPointsEarned.class);
    }

    @Test
    void redeemLoyaltyPointsProducesLoyaltyPointsRedeemedEvent() {
        List<LoyaltyWalletEvent> events = walletWithPoints(100).redeemPoints(owner, LoyaltyPoints.of(50), at);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(LoyaltyPointsRedeemed.class);
    }

    @Test
    void setRedemptionCadenceProducesRedemptionCadenceSetEvent() {
        List<LoyaltyWalletEvent> events = openWallet().setRedemptionCadence(RedemptionCadence.MONTHLY);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(RedemptionCadenceSet.class);
    }

    @Test
    void grantWalletAccessProducesWalletAccessGrantedEvent() {
        List<LoyaltyWalletEvent> events = openWallet().grantAccess(familyMember);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletAccessGranted.class);
    }

    @Test
    void revokeWalletAccessProducesWalletAccessRevokedEvent() {
        LoyaltyWallet wallet = openWallet();
        wallet.grantAccess(familyMember);

        List<LoyaltyWalletEvent> events = wallet.revokeAccess(familyMember);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletAccessRevoked.class);
    }

    @Test
    void resetRedemptionWindowProducesRedemptionWindowResetEvent() {
        List<LoyaltyWalletEvent> events = openWallet().resetRedemptionWindow(at);

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(RedemptionWindowReset.class);
    }

    @Test
    void deactivateWalletProducesWalletDeactivatedEvent() {
        List<LoyaltyWalletEvent> events = openWallet().deactivate();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletDeactivated.class);
    }

    @Test
    void closeWalletProducesWalletClosedEvent() {
        List<LoyaltyWalletEvent> events = openWallet().close();

        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(WalletClosed.class);
    }

    @Test
    void idempotentOpenProducesNoEvent() {
        LoyaltyWallet wallet = openWallet();

        List<LoyaltyWalletEvent> events = wallet.open(
                walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)
        );

        assertThat(events).isEmpty();
    }

    @Test
    void idempotentDeactivateProducesNoEvent() {
        LoyaltyWallet wallet = openWallet();
        wallet.deactivate();

        List<LoyaltyWalletEvent> events = wallet.deactivate();

        assertThat(events).isEmpty();
    }

    @Test
    void idempotentCloseProducesNoEvent() {
        LoyaltyWallet wallet = openWallet();
        wallet.close();

        List<LoyaltyWalletEvent> events = wallet.close();

        assertThat(events).isEmpty();
    }
}
