package com.razem.loyalty.loyaltyWallet.earningPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import com.razem.loyalty.membership.Tier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class PointsCalculatorTest {

    private final MemberId buyer = MemberId.random();
    private final MemberId referrer = MemberId.random();
    private final MemberId operator = MemberId.random();

    private PointsCalculator.PurchaseInformation purchase(Money purchaseValue, Tier tier, Channel channel, Instant at, List<LoyaltyPoints> bonuses, List<Beneficiary> beneficiaries) {
        return new PointsCalculator.PurchaseInformation(purchaseValue, buyer, channel, at, bonuses, beneficiaries, tier);
    }

    private PointsCalculator.PurchaseInformation defaultPurchase() {
        return purchase(Money.of(1000), Tier.STANDARD, Channel.IN_STORE, Instant.parse("2026-06-23T12:00:00Z"), List.of(), List.of());
    }

    @Nested
    class BuyerPointsTests {

        @ParameterizedTest
        @CsvSource({
                "80, 4",
                "200, 14",
                "1000, 100"
        })
        void appliesRateBracket(int purchaseValue, int expected) {
            PointsCalculator.PurchaseInformation info = purchase(Money.of(purchaseValue), Tier.STANDARD, Channel.IN_STORE,
                    Instant.parse("2026-06-23T12:00:00Z"), List.of(), List.of());

            assertThat(PointsCalculator.buyerPoints(info).value()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "STANDARD, 100",
                "SILVER, 110",
                "GOLD, 125",
                "PLATINUM, 150"
        })
        void appliesTierMultiplier(Tier tier, int expected) {
            PointsCalculator.PurchaseInformation info = purchase(Money.of(1000), tier, Channel.IN_STORE,
                    Instant.parse("2026-06-23T12:00:00Z"), List.of(), List.of());

            assertThat(PointsCalculator.buyerPoints(info).value()).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "IN_STORE, 100",
                "MOBILE_APP, 120",
                "WEB, 110"
        })
        void appliesChannelMultiplier(Channel channel, int expected) {
            PointsCalculator.PurchaseInformation info = purchase(Money.of(1000), Tier.STANDARD, channel,
                    Instant.parse("2026-06-23T12:00:00Z"), List.of(), List.of());

            assertThat(PointsCalculator.buyerPoints(info).value()).isEqualTo(expected);
        }

        @Test
        void appliesEveningMultiplierBetween18And22() {
            PointsCalculator.PurchaseInformation evening = purchase(Money.of(1000), Tier.STANDARD, Channel.IN_STORE,
                    Instant.parse("2026-06-23T17:00:00Z"), List.of(), List.of());  // 19:00 CEST = 17:00 UTC
            PointsCalculator.PurchaseInformation daytime = purchase(Money.of(1000), Tier.STANDARD, Channel.IN_STORE,
                    Instant.parse("2026-06-23T10:00:00Z"), List.of(), List.of());  // 12:00 CEST = 10:00 UTC

            assertThat(PointsCalculator.buyerPoints(evening).value()).isEqualTo(150);
            assertThat(PointsCalculator.buyerPoints(daytime).value()).isEqualTo(100);
        }

        @Test
        void addsPromotionBonuses() {
            PointsCalculator.PurchaseInformation info = purchase(Money.of(1000), Tier.STANDARD, Channel.IN_STORE,
                    Instant.parse("2026-06-23T12:00:00Z"),
                    List.of(LoyaltyPoints.of(10), LoyaltyPoints.of(5)), List.of());

            assertThat(PointsCalculator.buyerPoints(info).value()).isEqualTo(115);
        }
    }

    @Nested
    class CalculateTests {

        @Test
        void creditsOnlyBuyerWhenNoBeneficiaries() {
            ComponentBreakdown breakdown = PointsCalculator.calculate(defaultPurchase());

            assertThat(breakdown.components()).hasSize(1);
            assertThat(breakdown.components().get(0).member()).isEqualTo(buyer);
            assertThat(breakdown.components().get(0).role()).isEqualTo("Buyer");
            assertThat(breakdown.components().get(0).amount().value()).isEqualTo(100);
        }

        @Test
        void fansOutToOneComponentPerRecipient() {
            PointsCalculator.PurchaseInformation info = purchase(Money.of(1000), Tier.STANDARD, Channel.IN_STORE,
                    Instant.parse("2026-06-23T12:00:00Z"), List.of(),
                    List.of(
                            new Beneficiary(referrer, BeneficiaryRole.REFERRER),
                            new Beneficiary(operator, BeneficiaryRole.OPERATOR)
                    ));

            ComponentBreakdown breakdown = PointsCalculator.calculate(info);

            assertThat(breakdown.components()).hasSize(3);
            assertThat(breakdown.components().get(0).member()).isEqualTo(buyer);
            assertThat(breakdown.components().get(0).amount().value()).isEqualTo(100);
            assertThat(breakdown.components().get(1).member()).isEqualTo(referrer);
            assertThat(breakdown.components().get(1).amount().value()).isEqualTo(10); // 10% of buyer
            assertThat(breakdown.components().get(2).member()).isEqualTo(operator);
            assertThat(breakdown.components().get(2).amount().value()).isEqualTo(5);  // 5% of buyer
        }

        @Test
        void roundsEachBeneficiaryShare() {
            // buyer earns 14 -> referrer 14 * 0.1 = 1.4 -> rounds to 1
            PointsCalculator.PurchaseInformation info = purchase(Money.of(200), Tier.STANDARD, Channel.IN_STORE,
                    Instant.parse("2026-06-23T12:00:00Z"), List.of(),
                    List.of(new Beneficiary(referrer, BeneficiaryRole.REFERRER)));

            ComponentBreakdown breakdown = PointsCalculator.calculate(info);

            assertThat(breakdown.components()).hasSize(2);
            assertThat(breakdown.components().get(0).amount().value()).isEqualTo(14);
            assertThat(breakdown.components().get(1).amount().value()).isEqualTo(1);
        }
    }
}
