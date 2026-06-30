package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class WalletAccessTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final MemberId familyMember = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void ownerCanRedeemFromBalance() {
        LoyaltyWallet wallet = walletWithPoints(100);
        wallet.redeemPoints(owner, LoyaltyPoints.of(40), at);
        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
    }

    @Test
    void grantsAccessToFamilyMemberAndEmitsEvent() {
        LoyaltyWallet wallet = openWallet();

        List<LoyaltyWalletEvent> events = wallet.grantAccess(familyMember);

        assertThat(wallet.access().hasAccess(familyMember)).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(
                new WalletAccessGranted(walletNumber, owner, familyMember)
        );
    }

    @Test
    void revokesAccessFromFamilyMemberAndEmitsEvent() {
        LoyaltyWallet wallet = openWallet();
        wallet.grantAccess(familyMember);

        List<LoyaltyWalletEvent> events = wallet.revokeAccess(familyMember);

        assertThat(wallet.access().hasAccess(familyMember)).isFalse();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(
                new WalletAccessRevoked(walletNumber, owner, familyMember)
        );
    }

    @Test
    void grantedFamilyMemberCanRedeemFromBalance() {
        LoyaltyWallet wallet = walletWithPoints(100);
        wallet.grantAccess(familyMember);

        wallet.redeemPoints(familyMember, LoyaltyPoints.of(40), at);

        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
    }

    @Test
    void cannotRedeemWithoutAccess() {
        assertThatThrownBy(() ->
                walletWithPoints(100).redeemPoints(familyMember, LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
    }

    @Test
    void cannotRedeemAfterAccessRevoked() {
        LoyaltyWallet wallet = walletWithPoints(100);
        wallet.grantAccess(familyMember);
        wallet.revokeAccess(familyMember);

        assertThatThrownBy(() ->
                wallet.redeemPoints(familyMember, LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
    }

    @Test
    void cannotGrantAccessIfWalletNotActive() {
        assertThatThrownBy(() ->
                deactivated().grantAccess(familyMember)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
    }

    @Test
    void cannotRevokeAccessIfWalletNotActive() {
        assertThatThrownBy(() ->
                deactivated().revokeAccess(familyMember)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
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
}
