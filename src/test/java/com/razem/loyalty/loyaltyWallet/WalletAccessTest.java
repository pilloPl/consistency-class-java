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
    void initializesAccessForOwnerAndEmitsEvent() {
        WalletAccess access = WalletAccess.notExisting();

        List<LoyaltyWalletEvent> events = access.initialize(walletNumber, owner);

        assertThat(access.hasAccess(owner)).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new WalletAccessGranted(walletNumber, owner, owner));
    }

    @Test
    void grantsAccessToFamilyMemberAndEmitsEvent() {
        WalletAccess access = initializedAccess();

        List<LoyaltyWalletEvent> events = access.grant(familyMember);

        assertThat(access.hasAccess(familyMember)).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new WalletAccessGranted(walletNumber, owner, familyMember));
    }

    @Test
    void revokesAccessFromFamilyMemberAndEmitsEvent() {
        WalletAccess access = initializedAccess();
        access.grant(familyMember);

        List<LoyaltyWalletEvent> events = access.revoke(familyMember);

        assertThat(access.hasAccess(familyMember)).isFalse();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new WalletAccessRevoked(walletNumber, owner, familyMember));
    }

    @Test
    void ownerAlwaysHasAccess() {
        WalletAccess access = initializedAccess();

        assertThat(access.hasAccess(owner)).isTrue();
    }

    @Test
    void memberWithoutGrantHasNoAccess() {
        WalletAccess access = initializedAccess();

        assertThat(access.hasAccess(familyMember)).isFalse();
    }

    @Test
    void grantedMemberHasAccessAfterGrant() {
        WalletAccess access = initializedAccess();
        access.grant(familyMember);

        assertThat(access.hasAccess(familyMember)).isTrue();
    }

    @Test
    void revokedMemberHasNoAccessAfterRevoke() {
        WalletAccess access = initializedAccess();
        access.grant(familyMember);
        access.revoke(familyMember);

        assertThat(access.hasAccess(familyMember)).isFalse();
    }

    private WalletAccess initializedAccess() {
        WalletAccess access = WalletAccess.notExisting();
        access.initialize(walletNumber, owner);
        return access;
    }
}
