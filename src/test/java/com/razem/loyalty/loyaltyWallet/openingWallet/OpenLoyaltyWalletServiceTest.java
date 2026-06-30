package com.razem.loyalty.loyaltyWallet.openingWallet;

import com.razem.loyalty.loyaltyWallet.InMemoryLoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.OpenLoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.LoyaltyWalletOpened;
import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import com.razem.loyalty.loyaltyWallet.RedemptionLimit;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class OpenLoyaltyWalletServiceTest {

    private final InMemoryLoyaltyWalletRepository repository = new InMemoryLoyaltyWalletRepository();
    private final OpenLoyaltyWalletService service = new OpenLoyaltyWalletService(repository);

    @Test
    void opensNewWalletAndPersistsState() {
        WalletNumber walletNumber = WalletNumber.random();
        MemberId owner = MemberId.random();

        LoyaltyWallet.Active result = service.open(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5))
        );

        assertThat(result.walletNumber()).isEqualTo(walletNumber);
        assertThat(result.ownerId()).isEqualTo(owner);
        assertThat(result.pointsLimit().redemptionsLeft()).isEqualTo(5);

        LoyaltyWallet persisted = repository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(persisted).isInstanceOf(LoyaltyWallet.Active.class);
        assertThat(repository.getEvents(walletNumber)).hasSize(1);
        assertThat(repository.getEvents(walletNumber).get(0)).isInstanceOf(LoyaltyWalletOpened.class);
    }

    @Test
    void doesNotReopenAlreadyActiveWallet() {
        WalletNumber walletNumber = WalletNumber.random();
        MemberId owner = MemberId.random();

        service.open(new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)));
        // Opening again should not throw and should leave it unchanged
        service.open(new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)));

        LoyaltyWallet.Active persisted = (LoyaltyWallet.Active) repository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(persisted.cadence()).isEqualTo(RedemptionCadence.WEEKLY); // unchanged
        assertThat(repository.getEvents(walletNumber)).hasSize(1); // only one open event
    }
}
