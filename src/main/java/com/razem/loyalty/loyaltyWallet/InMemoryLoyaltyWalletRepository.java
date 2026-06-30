package com.razem.loyalty.loyaltyWallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryLoyaltyWalletRepository implements LoyaltyWalletRepository {

    private final Map<WalletNumber, LoyaltyWallet> wallets = new HashMap<>();
    private final Map<WalletNumber, List<LoyaltyWalletEvent>> events = new HashMap<>();

    @Override
    public Optional<LoyaltyWallet> findByWalletNumber(WalletNumber walletNumber) {
        return Optional.ofNullable(wallets.get(walletNumber));
    }

    @Override
    public void save(WalletNumber walletNumber, LoyaltyWallet wallet, List<LoyaltyWalletEvent> newEvents) {
        wallets.put(walletNumber, wallet);
        events.computeIfAbsent(walletNumber, k -> new ArrayList<>()).addAll(newEvents);
    }

    public List<LoyaltyWalletEvent> getEvents(WalletNumber walletNumber) {
        return events.getOrDefault(walletNumber, List.of());
    }
}
