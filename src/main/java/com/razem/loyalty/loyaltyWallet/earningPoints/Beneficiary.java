package com.razem.loyalty.loyaltyWallet.earningPoints;

import com.razem.loyalty.loyaltyWallet.access.MemberId;

public record Beneficiary(MemberId member, BeneficiaryRole role) {
}
