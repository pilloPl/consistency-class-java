package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class ResettingRedemptionWindowTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void resetsRedemptionCountKeepingBalanceAndEmitsEvent() {
        LoyaltyWallet.Active walletAfterRedeem = (LoyaltyWallet.Active) LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(10), at),
                walletWithPoints(100)
        ).state();
        assertThat(walletAfterRedeem.pointsLimit().redemptionsLeft()).isEqualTo(4);

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.resetRedemptionWindow(
                new ResetRedemptionWindow(walletNumber, at),
                walletAfterRedeem
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().redemptionsLeft()).isEqualTo(5);
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(90);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(
                new RedemptionWindowReset(walletNumber, owner, at)
        );
    }

    @Test
    void cannotResetWindowIfWalletNotActive() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.resetRedemptionWindow(
                        new ResetRedemptionWindow(walletNumber, at),
                        deactivated()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
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
}
