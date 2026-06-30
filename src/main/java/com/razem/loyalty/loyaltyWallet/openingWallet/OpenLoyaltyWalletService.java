package com.razem.loyalty.loyaltyWallet.openingWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import com.razem.loyalty.loyaltyWallet.RedemptionLimit;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.util.List;

public class OpenLoyaltyWalletService {

    private final LoyaltyWalletRepository repository;

    public OpenLoyaltyWalletService(LoyaltyWalletRepository repository) {
        this.repository = repository;
    }

    public LoyaltyWallet open(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount
    ) {
        LoyaltyWallet wallet = repository.findByWalletNumber(walletNumber)
                .orElse(LoyaltyWallet.notExisting());

        List<LoyaltyWalletEvent> events = wallet.open(walletNumber, ownerId, cadence, maxRedemptionCount);

        repository.save(walletNumber, wallet, events);
        return wallet;
    }
}
