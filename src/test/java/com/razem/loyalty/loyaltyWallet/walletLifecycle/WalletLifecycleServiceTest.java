package com.razem.loyalty.loyaltyWallet.walletLifecycle;

import com.razem.loyalty.loyaltyWallet.InMemoryLoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.WalletClosed;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.WalletDeactivated;
import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import com.razem.loyalty.loyaltyWallet.RedemptionLimit;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import com.razem.loyalty.loyaltyWallet.openingWallet.OpenLoyaltyWalletService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class WalletLifecycleServiceTest {

    private final InMemoryLoyaltyWalletRepository repository = new InMemoryLoyaltyWalletRepository();
    private final OpenLoyaltyWalletService openService = new OpenLoyaltyWalletService(repository);
    private final DeactivateWalletService deactivateService = new DeactivateWalletService(repository);
    private final CloseWalletService closeService = new CloseWalletService(repository);

    @Test
    void deactivatesWalletAndPersistsEvent() {
        WalletNumber walletNumber = openedWallet();

        LoyaltyWallet result = deactivateService.deactivate(walletNumber);

        assertThat(result.isDeactivated()).isTrue();
        LoyaltyWallet persisted = repository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(persisted.isDeactivated()).isTrue();
        long deactivatedEvents = repository.getEvents(walletNumber).stream()
                .filter(e -> e instanceof WalletDeactivated).count();
        assertThat(deactivatedEvents).isEqualTo(1);
    }

    @Test
    void closesWalletAndPersistsEvent() {
        WalletNumber walletNumber = openedWallet();

        LoyaltyWallet result = closeService.close(walletNumber);

        assertThat(result.isClosed()).isTrue();
        LoyaltyWallet persisted = repository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(persisted.isClosed()).isTrue();
        long closedEvents = repository.getEvents(walletNumber).stream()
                .filter(e -> e instanceof WalletClosed).count();
        assertThat(closedEvents).isEqualTo(1);
    }

    @Test
    void deactivateThenCloseWallet() {
        WalletNumber walletNumber = openedWallet();

        deactivateService.deactivate(walletNumber);
        LoyaltyWallet result = closeService.close(walletNumber);

        assertThat(result.isClosed()).isTrue();
    }

    @Test
    void cannotCloseNotExistingWallet() {
        WalletNumber walletNumber = WalletNumber.random();

        assertThatThrownBy(() -> closeService.close(walletNumber))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Wallet doesn't exist");
    }

    private WalletNumber openedWallet() {
        WalletNumber walletNumber = WalletNumber.random();
        openService.open(walletNumber, MemberId.random(), RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        return walletNumber;
    }
}
