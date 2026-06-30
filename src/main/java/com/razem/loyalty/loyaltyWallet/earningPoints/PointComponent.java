package com.razem.loyalty.loyaltyWallet.earningPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

public record PointComponent(MemberId member, String role, LoyaltyPoints amount) {
}
