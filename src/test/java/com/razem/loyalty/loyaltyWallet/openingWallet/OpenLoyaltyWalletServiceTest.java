package com.razem.loyalty.loyaltyWallet.openingWallet;

import com.razem.loyalty.loyaltyWallet.InMemoryLoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
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

        LoyaltyWallet result = service.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));

        assertThat(result.walletNumber()).isEqualTo(walletNumber);
        assertThat(result.ownerId()).isEqualTo(owner);
        assertThat(result.pointsLimit().redemptionsLeft()).isEqualTo(5);

        LoyaltyWallet persisted = repository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(persisted.isActive()).isTrue();
        assertThat(repository.getEvents(walletNumber)).hasSize(1);
        assertThat(repository.getEvents(walletNumber).get(0)).isInstanceOf(LoyaltyWalletOpened.class);
    }

    @Test
    void doesNotReopenAlreadyActiveWallet() {
        WalletNumber walletNumber = WalletNumber.random();
        MemberId owner = MemberId.random();

        service.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        service.open(walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3));

        LoyaltyWallet persisted = repository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(persisted.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
        assertThat(repository.getEvents(walletNumber)).hasSize(1);
    }
}
