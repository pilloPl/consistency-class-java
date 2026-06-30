package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class WalletAccessTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final MemberId familyMember = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void ownerCanRedeemFromBalance() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                walletWithPoints(100)
        );
        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
    }

    @Test
    void grantsAccessToFamilyMemberAndEmitsEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.grantWalletAccess(
                new GrantWalletAccess(walletNumber, familyMember),
                openWallet()
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.access().hasAccess(familyMember)).isTrue();
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(
                new WalletAccessGranted(walletNumber, owner, familyMember)
        );
    }

    @Test
    void revokesAccessFromFamilyMemberAndEmitsEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantResult = LoyaltyWalletDecider.grantWalletAccess(
                new GrantWalletAccess(walletNumber, familyMember),
                openWallet()
        );
        LoyaltyWallet.Active shared = (LoyaltyWallet.Active) grantResult.state();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> revokeResult = LoyaltyWalletDecider.revokeWalletAccess(
                new RevokeWalletAccess(walletNumber, familyMember),
                shared
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) revokeResult.state();
        assertThat(active.access().hasAccess(familyMember)).isFalse();
        assertThat(revokeResult.events()).hasSize(1);
        assertThat(revokeResult.events().get(0)).isEqualTo(
                new WalletAccessRevoked(walletNumber, owner, familyMember)
        );
    }

    @Test
    void grantedFamilyMemberCanRedeemFromBalance() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantResult = LoyaltyWalletDecider.grantWalletAccess(
                new GrantWalletAccess(walletNumber, familyMember),
                walletWithPoints(100)
        );
        LoyaltyWallet.Active shared = (LoyaltyWallet.Active) grantResult.state();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> redeemResult = LoyaltyWalletDecider.redeemLoyaltyPoints(
                new RedeemLoyaltyPoints(walletNumber, familyMember, LoyaltyPoints.of(40), at),
                shared
        );

        LoyaltyWallet.Active active = (LoyaltyWallet.Active) redeemResult.state();
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
    }

    @Test
    void cannotRedeemWithoutAccess() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, familyMember, LoyaltyPoints.of(40), at),
                        walletWithPoints(100)
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
    }

    @Test
    void cannotRedeemAfterAccessRevoked() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantResult = LoyaltyWalletDecider.grantWalletAccess(
                new GrantWalletAccess(walletNumber, familyMember),
                walletWithPoints(100)
        );
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> revokeResult = LoyaltyWalletDecider.revokeWalletAccess(
                new RevokeWalletAccess(walletNumber, familyMember),
                (LoyaltyWallet.Active) grantResult.state()
        );

        assertThatThrownBy(() ->
                LoyaltyWalletDecider.redeemLoyaltyPoints(
                        new RedeemLoyaltyPoints(walletNumber, familyMember, LoyaltyPoints.of(40), at),
                        revokeResult.state()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
    }

    @Test
    void cannotGrantAccessIfWalletNotActive() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.grantWalletAccess(
                        new GrantWalletAccess(walletNumber, familyMember),
                        deactivated()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
    }

    @Test
    void cannotRevokeAccessIfWalletNotActive() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.revokeWalletAccess(
                        new RevokeWalletAccess(walletNumber, familyMember),
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
