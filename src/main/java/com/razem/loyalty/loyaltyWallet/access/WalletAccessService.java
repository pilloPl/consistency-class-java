package com.razem.loyalty.loyaltyWallet.access;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.WalletAccess;
import com.razem.loyalty.loyaltyWallet.WalletAccessRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;

import java.util.List;

public class WalletAccessService {

    private final WalletAccessRepository accessRepository;

    public WalletAccessService(WalletAccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    public void grant(WalletNumber walletNumber, MemberId memberId) {
        WalletAccess access = accessRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new IllegalStateException("Wallet doesn't exist"));

        List<LoyaltyWalletEvent> events = access.grant(memberId);
        accessRepository.save(walletNumber, access, events);
    }

    public void revoke(WalletNumber walletNumber, MemberId memberId) {
        WalletAccess access = accessRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new IllegalStateException("Wallet doesn't exist"));

        List<LoyaltyWalletEvent> events = access.revoke(memberId);
        accessRepository.save(walletNumber, access, events);
    }
}
