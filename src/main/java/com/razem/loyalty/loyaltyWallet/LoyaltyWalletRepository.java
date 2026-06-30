package com.razem.loyalty.loyaltyWallet;

import java.util.Optional;

public interface LoyaltyWalletRepository {
    Optional<LoyaltyWallet> findByWalletNumber(WalletNumber walletNumber);
    void save(WalletNumber walletNumber, LoyaltyWallet wallet, java.util.List<LoyaltyWalletEvent> events);
}
