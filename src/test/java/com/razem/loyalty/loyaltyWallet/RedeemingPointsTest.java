package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class RedeemingPointsTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void redeemsPointsOnActiveWalletAndEmitsEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                walletWithPoints(100)
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
        assertThat(active.pointsLimit().redemptionsLeft()).isEqualTo(4);
        assertThat(result.events()).hasSize(1);
        LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) result.events().get(0);
        assertThat(event.walletNumber()).isEqualTo(walletNumber);
        assertThat(event.ownerId()).isEqualTo(owner);
        assertThat(event.byMemberId()).isEqualTo(owner);
        assertThat(event.points().value()).isEqualTo(40);
        assertThat(event.burned().value()).isEqualTo(40);
    }

    @Test
    void burnsPolicyAmountFromBalanceWhileRecordingRedeemedAmount() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(100), LoyaltyPoints.of(95), at),
                walletWithPoints(100)
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(5);
        LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) result.events().get(0);
        assertThat(event.points().value()).isEqualTo(100);
        assertThat(event.burned().value()).isEqualTo(95);
    }

    @Test
    void cannotRedeemWhenRedemptionWindowExhausted() {
        LoyaltyWallet.Active wallet = (LoyaltyWallet.Active) LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(1)),
                LoyaltyWallet.initial()
        ).state();
        wallet = (LoyaltyWallet.Active) LoyaltyWalletDecider.earnLoyaltyPoints(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                wallet
        ).state();
        wallet = (LoyaltyWallet.Active) LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(10), at),
                wallet
        ).state();

        LoyaltyWallet.Active exhausted = wallet;
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(10), at),
                        exhausted
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Redemption window exhausted");
    }

    @Test
    void cannotRedeemMoreThanAvailable() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(50), at),
                        walletWithPoints(20)
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not enough points to redeem");
    }

    @Test
    void cannotRedeemOnNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                        LoyaltyWallet.initial()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    @Test
    void cannotRedeemOnDeactivatedWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                        deactivated()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
    }

    @Test
    void cannotRedeemOnClosedWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
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
