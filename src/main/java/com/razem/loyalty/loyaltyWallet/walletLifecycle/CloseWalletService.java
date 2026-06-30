package com.razem.loyalty.loyaltyWallet.walletLifecycle;

import com.razem.loyalty.loyaltyWallet.DecideResult;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.CloseWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletDecider;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;

public class CloseWalletService {

    private final LoyaltyWalletRepository repository;

    public CloseWalletService(LoyaltyWalletRepository repository) {
        this.repository = repository;
    }

    public LoyaltyWallet close(WalletNumber walletNumber) {
        LoyaltyWallet current = repository.findByWalletNumber(walletNumber)
                .orElse(LoyaltyWallet.initial());

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber), current);

        repository.save(walletNumber, result.state(), result.events());
        return result.state();
    }
}
