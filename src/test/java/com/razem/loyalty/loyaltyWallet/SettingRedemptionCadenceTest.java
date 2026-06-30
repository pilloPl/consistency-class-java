package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class SettingRedemptionCadenceTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();

    @Test
    void changesCadenceOnActiveWalletAndEmitsEvent() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.setRedemptionCadence(
                new SetRedemptionCadence(walletNumber, RedemptionCadence.MONTHLY),
                openWallet()
        );

        assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
        LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
        assertThat(active.cadence()).isEqualTo(RedemptionCadence.MONTHLY);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0)).isEqualTo(
                new RedemptionCadenceSet(walletNumber, owner, RedemptionCadence.MONTHLY)
        );
    }

    @Test
    void cannotSetCadenceOnNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWalletDecider.setRedemptionCadence(
                        new SetRedemptionCadence(walletNumber, RedemptionCadence.MONTHLY),
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
}
