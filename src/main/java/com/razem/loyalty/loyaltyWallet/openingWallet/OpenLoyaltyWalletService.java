package com.razem.loyalty.loyaltyWallet.openingWallet;

import com.razem.loyalty.loyaltyWallet.DecideResult;
import com.razem.loyalty.loyaltyWallet.InMemoryLoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.OpenLoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletDecider;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;

public class OpenLoyaltyWalletService {

    private final LoyaltyWalletRepository repository;

    public OpenLoyaltyWalletService(LoyaltyWalletRepository repository) {
        this.repository = repository;
    }

    public LoyaltyWallet.Active open(OpenLoyaltyWallet command) {
        LoyaltyWallet current = repository.findByWalletNumber(command.walletNumber())
                .orElse(LoyaltyWallet.initial());

        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(command, current);

        repository.save(command.walletNumber(), result.state(), result.events());

        if (result.state() instanceof LoyaltyWallet.Active active) {
            return active;
        }
        throw new IllegalStateException("Wallet could not be opened");
    }
}
