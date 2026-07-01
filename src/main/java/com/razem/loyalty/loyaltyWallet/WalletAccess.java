package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WalletAccess {

    private WalletNumber walletNumber;
    private MemberId ownerId;
    private Set<MemberId> members = new HashSet<>();
    private boolean initialized = false;

    public static WalletAccess notExisting() {
        return new WalletAccess();
    }

    public List<LoyaltyWalletEvent> initialize(WalletNumber walletNumber, MemberId ownerId) {
        if (initialized) {
            return List.of();
        }
        this.walletNumber = walletNumber;
        this.ownerId = ownerId;
        this.members.add(ownerId);
        this.initialized = true;
        return List.of(new WalletAccessGranted(walletNumber, ownerId, ownerId));
    }

    public List<LoyaltyWalletEvent> grant(MemberId memberId) {
        assertInitialized();
        members.add(memberId);
        return List.of(new WalletAccessGranted(walletNumber, ownerId, memberId));
    }

    public List<LoyaltyWalletEvent> revoke(MemberId memberId) {
        assertInitialized();
        members.remove(memberId);
        return List.of(new WalletAccessRevoked(walletNumber, ownerId, memberId));
    }

    public boolean hasAccess(MemberId memberId) {
        return members.contains(memberId);
    }

    private void assertInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Wallet access not initialized");
        }
    }
}
