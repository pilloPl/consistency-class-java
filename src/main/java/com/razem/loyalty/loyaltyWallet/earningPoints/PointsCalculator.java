package com.razem.loyalty.loyaltyWallet.earningPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.membership.Tier;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * What the calculator consumes: the recorded facts enriched with the buyer's tier.
 */
public class PointsCalculator {

    public record PurchaseInformation(
            Money purchaseValue,
            com.razem.loyalty.loyaltyWallet.access.MemberId buyer,
            Channel channel,
            java.time.Instant at,
            List<LoyaltyPoints> promotionBonuses,
            List<Beneficiary> beneficiaries,
            Tier tier
    ) {
        public static PurchaseInformation of(Purchase purchase, Tier tier) {
            return new PurchaseInformation(
                    purchase.purchaseValue(),
                    purchase.buyer(),
                    purchase.channel(),
                    purchase.at(),
                    purchase.promotionBonuses(),
                    purchase.beneficiaries(),
                    tier
            );
        }
    }

    private static double rate(Money purchaseValue) {
        int value = purchaseValue.value();
        if (value < 100) return 0.05;
        if (value < 500) return 0.07;
        return 0.1;
    }

    private static double tierMultiplier(Tier tier) {
        return switch (tier) {
            case STANDARD -> 1.0;
            case SILVER -> 1.1;
            case GOLD -> 1.25;
            case PLATINUM -> 1.5;
        };
    }

    private static double channelMultiplier(Channel channel) {
        return switch (channel) {
            case IN_STORE -> 1.0;
            case WEB -> 1.1;
            case MOBILE_APP -> 1.2;
        };
    }

    private static double shareOfBuyer(BeneficiaryRole role) {
        return switch (role) {
            case REFERRER -> 0.1;
            case OPERATOR -> 0.05;
        };
    }

    private static boolean isEvening(java.time.Instant at) {
        int hour = at.atZone(ZoneId.systemDefault()).getHour();
        return hour >= 18 && hour < 22;
    }

    public static LoyaltyPoints buyerPoints(PurchaseInformation purchase) {
        double points = purchase.purchaseValue().value() * rate(purchase.purchaseValue());
        points *= tierMultiplier(purchase.tier());
        points *= channelMultiplier(purchase.channel());
        if (isEvening(purchase.at())) {
            points *= 1.5;
        }
        int bonuses = purchase.promotionBonuses().stream()
                .mapToInt(LoyaltyPoints::value)
                .sum();
        points += bonuses;
        return LoyaltyPoints.of((int) Math.round(points));
    }

    public static ComponentBreakdown calculate(PurchaseInformation purchase) {
        LoyaltyPoints base = buyerPoints(purchase);

        List<PointComponent> components = new ArrayList<>();
        components.add(new PointComponent(purchase.buyer(), "Buyer", base));

        for (Beneficiary beneficiary : purchase.beneficiaries()) {
            int amount = (int) Math.round(base.value() * shareOfBuyer(beneficiary.role()));
            components.add(new PointComponent(beneficiary.member(), beneficiary.role().name(), LoyaltyPoints.of(amount)));
        }

        return new ComponentBreakdown(components);
    }
}
