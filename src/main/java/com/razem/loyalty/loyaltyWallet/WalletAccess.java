package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.util.HashSet;
import java.util.Set;

public record WalletAccess(Set<MemberId> members) {
    public static WalletAccess of(MemberId owner) {
        return new WalletAccess(Set.of(owner));
    }

    public WalletAccess grant(MemberId memberId) {
        Set<MemberId> newMembers = new HashSet<>(members);
        newMembers.add(memberId);
        return new WalletAccess(Set.copyOf(newMembers));
    }

    public WalletAccess revoke(MemberId memberId) {
        Set<MemberId> newMembers = new HashSet<>(members);
        newMembers.remove(memberId);
        return new WalletAccess(Set.copyOf(newMembers));
    }

    public boolean hasAccess(MemberId memberId) {
        return members.contains(memberId);
    }
}
