package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ClosingTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();

    @Test
    void closesActiveWalletAndEmitsEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber),
                openWallet()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Closed.class);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(new WalletClosed(walletNumber));
    }

    @Test
    void closesDeactivatedWallet() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber),
                deactivated()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Closed.class);
    }

    @Test
    void leavesAlreadyClosedWalletUnchangedWithoutEvents() {
        LoyaltyWallet.Closed closedWallet = closed();

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber),
                closedWallet
        );

        assertThat(result.state()).isSameAs(closedWallet);
        assertThat(result.events()).isEmpty();
    }

    @Test
    void cannotCloseNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.closeWallet(
                        new CloseWallet(walletNumber),
                        LoyaltyWallet.initial()
                )
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
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
