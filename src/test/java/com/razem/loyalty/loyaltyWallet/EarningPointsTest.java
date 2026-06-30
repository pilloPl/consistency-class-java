package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class EarningPointsTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void earnsPointsOnActiveWalletAndEmitsEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.earnLoyaltyPoints(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                openWallet()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(100);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(
                new LoyaltyPointsEarned(walletNumber, owner, LoyaltyPoints.of(100), at)
        );
    }

    @Test
    void cannotEarnOnNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.earnLoyaltyPoints(
                        new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                        LoyaltyWallet.initial()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    @Test
    void cannotEarnOnDeactivatedWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.earnLoyaltyPoints(
                        new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                        deactivated()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
    }

    @Test
    void cannotEarnOnClosedWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.earnLoyaltyPoints(
                        new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
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
