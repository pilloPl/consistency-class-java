package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class DeactivatingTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void deactivatesActiveWalletKeepingDataAndEmitsEvent() {
        LoyaltyWallet.Active walletAfterRedeem = (LoyaltyWallet.Active) LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(10), at),
                walletWithPoints(100)
        ).state();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber),
                walletAfterRedeem
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Deactivated.class);
        LoyaltyWallet.Deactivated deactivated = (LoyaltyWallet.Deactivated) result.state();
        assertThat(deactivated.walletNumber()).isEqualTo(walletNumber);
        assertThat(deactivated.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
        assertThat(deactivated.pointsLimit().availablePoints().value()).isEqualTo(90);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(new WalletDeactivated(walletNumber, owner));
    }

    @Test
    void leavesAlreadyDeactivatedWalletUnchangedWithoutEvents() {
        LoyaltyWallet.Deactivated deactivatedWallet = deactivated();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber),
                deactivatedWallet
        );

        assertThat(result.state()).isSameAs(deactivatedWallet);
        assertThat(result.events()).isEmpty();
    }

    @Test
    void cannotDeactivateNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.deactivateWallet(
                        new DeactivateWallet(walletNumber),
                        LoyaltyWallet.initial()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    @Test
    void cannotDeactivateClosedWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.deactivateWallet(
                        new DeactivateWallet(walletNumber),
                        closed()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
    }

    private LoyaltyWallet.Active openWallet() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                LoyaltyWallet.initial()
        );
        return (LoyaltyWallet.Active) result.state();
    }

    private LoyaltyWallet.Active walletWithPoints(int points) {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.earnLoyaltyPoints(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(points), at),
                openWallet()
        );
        return (LoyaltyWallet.Active) result.state();
    }

    private LoyaltyWallet.Deactivated deactivated() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber),
                openWallet()
        );
        return (LoyaltyWallet.Deactivated) result.state();
    }

    private LoyaltyWallet.Closed closed() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber),
                openWallet()
        );
        return (LoyaltyWallet.Closed) result.state();
    }
}
