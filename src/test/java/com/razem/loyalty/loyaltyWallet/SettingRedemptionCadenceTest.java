package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class SettingRedemptionCadenceTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();

    @Test
    void changesCadenceOnActiveWalletAndEmitsEvent() {
        LoyaltyWallet wallet = openWallet();

        List<LoyaltyWalletEvent> events = wallet.setRedemptionCadence(RedemptionCadence.MONTHLY);

        assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.MONTHLY);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(
                new RedemptionCadenceSet(walletNumber, owner, RedemptionCadence.MONTHLY)
        );
    }

    @Test
    void cannotSetCadenceOnNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWallet.notExisting().setRedemptionCadence(RedemptionCadence.MONTHLY)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    private LoyaltyWallet openWallet() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        return wallet;
    }
}
