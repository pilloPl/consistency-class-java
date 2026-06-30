package com.razem.loyalty.loyaltyWallet.walletLifecycle;

import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;

import java.util.List;

public class CloseWalletService {

    private final LoyaltyWalletRepository repository;

    public CloseWalletService(LoyaltyWalletRepository repository) {
        this.repository = repository;
    }

    public LoyaltyWallet close(WalletNumber walletNumber) {
        LoyaltyWallet wallet = repository.findByWalletNumber(walletNumber)
                .orElse(LoyaltyWallet.notExisting());

        List<LoyaltyWalletEvent> events = wallet.close();

        repository.save(walletNumber, wallet, events);
        return wallet;
    }
}
