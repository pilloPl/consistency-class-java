package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class OpeningTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();

    @Test
    void opensNotExistingWalletAndEmitsEvent() {
        LoyaltyWallet initial = LoyaltyWallet.initial();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                initial
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.walletNumber()).isEqualTo(walletNumber);
        assertThat(active.ownerId()).isEqualTo(owner);
        assertThat(active.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
        assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(0);
        assertThat(active.pointsLimit().redemptionsLeft()).isEqualTo(5);
        assertThat(active.access().hasAccess(owner)).isTrue();
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(new LoyaltyWalletOpened(walletNumber, owner));
    }

    @Test
    void leavesAlreadyActiveWalletUnchangedWithoutEvents() {
        LoyaltyWallet.Active activeWallet = openWallet();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)),
                activeWallet
        );

        assertThat(result.state()).isSameAs(activeWallet);
        assertThat(result.events()).isEmpty();
    }

    @Test
    void leavesDeactivatedWalletUnchangedWithoutEvents() {
        LoyaltyWallet.Deactivated deactivatedWallet = deactivated();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                deactivatedWallet
        );

        assertThat(result.state()).isSameAs(deactivatedWallet);
        assertThat(result.events()).isEmpty();
    }

    @Test
    void leavesClosedWalletUnchangedWithoutEvents() {
        LoyaltyWallet.Closed closedWallet = closed();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                closedWallet
        );

        assertThat(result.state()).isSameAs(closedWallet);
        assertThat(result.events()).isEmpty();
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
