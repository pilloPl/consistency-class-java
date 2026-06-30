package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ClosingTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();

    @Test
    void closesActiveWalletAndEmitsEvent() {
        LoyaltyWallet wallet = openWallet();

        List<LoyaltyWalletEvent> events = wallet.close();

        assertThat(wallet.isClosed()).isTrue();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isEqualTo(new WalletClosed(walletNumber));
    }

    @Test
    void closesDeactivatedWallet() {
        LoyaltyWallet wallet = deactivated();

        List<LoyaltyWalletEvent> events = wallet.close();

        assertThat(wallet.isClosed()).isTrue();
        assertThat(events).hasSize(1);
    }

    @Test
    void leavesAlreadyClosedWalletUnchangedWithoutEvents() {
        LoyaltyWallet wallet = closed();

        List<LoyaltyWalletEvent> events = wallet.close();

        assertThat(events).isEmpty();
        assertThat(wallet.isClosed()).isTrue();
    }

    @Test
    void cannotCloseNotExistingWallet() {
        assertThatThrownBy(() ->
                LoyaltyWallet.notExisting().close()
        ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
    }

    private LoyaltyWallet openWallet() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        return wallet;
    }

    private LoyaltyWallet deactivated() {
        LoyaltyWallet wallet = openWallet();
        wallet.deactivate();
        return wallet;
    }

    private LoyaltyWallet closed() {
        LoyaltyWallet wallet = openWallet();
        wallet.close();
        return wallet;
    }
}
