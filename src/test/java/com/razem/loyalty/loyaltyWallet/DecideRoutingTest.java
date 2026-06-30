package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class DecideRoutingTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void routesOpenLoyaltyWallet() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                LoyaltyWallet.initial()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.walletNumber()).isEqualTo(walletNumber);
    }

    @Test
    void routesEarnLoyaltyPoints() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                openWallet()
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(100);
    }

    @Test
    void routesRedeemLoyaltyPoints() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                walletWithPoints(100)
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
    }

    @Test
    void routesDeactivateWallet() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                new DeactivateWallet(walletNumber),
                openWallet()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Deactivated.class);
    }

    @Test
    void routesCloseWallet() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                new CloseWallet(walletNumber),
                openWallet()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Closed.class);
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
}
