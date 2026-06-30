package com.razem.loyalty.membership;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

public record Member(MemberId memberId, Tier tier) {
}
