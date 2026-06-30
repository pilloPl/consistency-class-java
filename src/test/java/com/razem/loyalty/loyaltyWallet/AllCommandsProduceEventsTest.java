package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Verifies that every state-changing command produces at least one event.
 * Idempotent transitions (e.g. already-deactivated) are allowed to return no events.
 */
class AllCommandsProduceEventsTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final MemberId familyMember = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void openLoyaltyWalletProducesLoyaltyWalletOpenedEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                LoyaltyWallet.initial()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(LoyaltyWalletOpened.class);
    }

    @Test
    void earnLoyaltyPointsProducesLoyaltyPointsEarnedEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.earnLoyaltyPoints(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                openWallet()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(LoyaltyPointsEarned.class);
    }

    @Test
    void redeemLoyaltyPointsProducesLoyaltyPointsRedeemedEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(50), at),
                walletWithPoints(100)
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(LoyaltyPointsRedeemed.class);
    }

    @Test
    void setRedemptionCadenceProducesRedemptionCadenceSetEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.setRedemptionCadence(
                new SetRedemptionCadence(walletNumber, RedemptionCadence.MONTHLY),
                openWallet()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(RedemptionCadenceSet.class);
    }

    @Test
    void grantWalletAccessProducesWalletAccessGrantedEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.grantWalletAccess(
                new GrantWalletAccess(walletNumber, familyMember),
                openWallet()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(WalletAccessGranted.class);
    }

    @Test
    void revokeWalletAccessProducesWalletAccessRevokedEvent() {
        LoyaltyWallet.Active shared = (LoyaltyWallet.Active) LoyaltyWalletDecider.grantWalletAccess(
                new GrantWalletAccess(walletNumber, familyMember),
                openWallet()
        ).state();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.revokeWalletAccess(
                new RevokeWalletAccess(walletNumber, familyMember),
                shared
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(WalletAccessRevoked.class);
    }

    @Test
    void resetRedemptionWindowProducesRedemptionWindowResetEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.resetRedemptionWindow(
                new ResetRedemptionWindow(walletNumber, at),
                openWallet()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(RedemptionWindowReset.class);
    }

    @Test
    void deactivateWalletProducesWalletDeactivatedEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber),
                openWallet()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(WalletDeactivated.class);
    }

    @Test
    void closeWalletProducesWalletClosedEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber),
                openWallet()
        );

        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isInstanceOf(WalletClosed.class);
    }

    @Test
    void idempotentOpenProducesNoEvent() {
        LoyaltyWallet.Active active = openWallet();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)),
                active
        );

        assertThat(result.events()).isEmpty();
    }

    @Test
    void idempotentDeactivateProducesNoEvent() {
        LoyaltyWallet.Deactivated deactivated = (LoyaltyWallet.Deactivated) LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber), openWallet()).state();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber), deactivated);

        assertThat(result.events()).isEmpty();
    }

    @Test
    void idempotentCloseProducesNoEvent() {
        LoyaltyWallet.Closed closed = (LoyaltyWallet.Closed) LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber), openWallet()).state();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber), closed);

        assertThat(result.events()).isEmpty();
    }

    private LoyaltyWallet.Active openWallet() {
        return (LoyaltyWallet.Active) LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                LoyaltyWallet.initial()
        ).state();
    }

    private LoyaltyWallet.Active walletWithPoints(int points) {
        return (LoyaltyWallet.Active) LoyaltyWalletDecider.earnLoyaltyPoints(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(points), at),
                openWallet()
        ).state();
    }
}
