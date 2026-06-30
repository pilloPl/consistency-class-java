package com.razem.loyalty.loyaltyWallet.earningPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import com.razem.loyalty.membership.Tier;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PurchaseTest {

    @Test
    void carriesEveryRecordedFieldThroughAndAttachesTier() {
        MemberId buyer = MemberId.random();
        MemberId referrerMember = MemberId.random();
        Instant at = Instant.parse("2026-06-23T19:00:00Z");
        Money purchaseValue = Money.of(1000);
        List<LoyaltyPoints> promotionBonuses = List.of(LoyaltyPoints.of(10));
        List<Beneficiary> beneficiaries = List.of(new Beneficiary(referrerMember, BeneficiaryRole.REFERRER));

        Purchase purchase = new Purchase(purchaseValue, buyer, Channel.MOBILE_APP, at, promotionBonuses, beneficiaries);
        PointsCalculator.PurchaseInformation info = PointsCalculator.PurchaseInformation.of(purchase, Tier.GOLD);

        assertThat(info.purchaseValue()).isEqualTo(purchaseValue);
        assertThat(info.buyer()).isEqualTo(buyer);
        assertThat(info.channel()).isEqualTo(Channel.MOBILE_APP);
        assertThat(info.at()).isEqualTo(at);
        assertThat(info.promotionBonuses()).isEqualTo(promotionBonuses);
        assertThat(info.beneficiaries()).isEqualTo(beneficiaries);
        assertThat(info.tier()).isEqualTo(Tier.GOLD);
    }
}
