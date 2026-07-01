package com.razem.loyalty.loyaltyWallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryWalletAccessRepository implements WalletAccessRepository {

    private final Map<WalletNumber, WalletAccess> store = new HashMap<>();
    private final Map<WalletNumber, List<LoyaltyWalletEvent>> events = new HashMap<>();

    @Override
    public Optional<WalletAccess> findByWalletNumber(WalletNumber walletNumber) {
        return Optional.ofNullable(store.get(walletNumber));
    }

    @Override
    public void save(WalletNumber walletNumber, WalletAccess access, List<LoyaltyWalletEvent> newEvents) {
        store.put(walletNumber, access);
        events.computeIfAbsent(walletNumber, k -> new ArrayList<>()).addAll(newEvents);
    }

    public List<LoyaltyWalletEvent> getEvents(WalletNumber walletNumber) {
        return events.getOrDefault(walletNumber, List.of());
    }
}
