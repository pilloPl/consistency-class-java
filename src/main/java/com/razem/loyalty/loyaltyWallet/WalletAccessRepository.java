package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.util.List;
import java.util.Optional;

public interface WalletAccessRepository {
    Optional<WalletAccess> findByWalletNumber(WalletNumber walletNumber);
    void save(WalletNumber walletNumber, WalletAccess access, List<LoyaltyWalletEvent> events);
}
