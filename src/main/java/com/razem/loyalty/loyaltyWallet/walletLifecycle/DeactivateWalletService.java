package com.razem.loyalty.loyaltyWallet.walletLifecycle;

import com.razem.loyalty.loyaltyWallet.DecideResult;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.DeactivateWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletDecider;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;

public class DeactivateWalletService {

    private final LoyaltyWalletRepository repository;

    public DeactivateWalletService(LoyaltyWalletRepository repository) {
        this.repository = repository;
    }

    public LoyaltyWallet deactivate(WalletNumber walletNumber) {
        LoyaltyWallet current = repository.findByWalletNumber(walletNumber)
                .orElse(LoyaltyWallet.initial());

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber), current);

        repository.save(walletNumber, result.state(), result.events());
        return result.state();
    }
}
