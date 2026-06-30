package com.razem.loyalty.loyaltyWallet.monthlySummary;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class MonthlySummaryTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant june = Instant.parse("2026-06-23T12:00:00Z");

    private MonthlySummary fold(LoyaltyWalletEvent... events) {
        MonthlySummary result = null;
        for (LoyaltyWalletEvent event : events) {
            result = MonthlySummary.evolve(result, event);
        }
        return result;
    }

    private LoyaltyPointsEarned earned(int points, Instant at) {
        return new LoyaltyPointsEarned(walletNumber, owner, LoyaltyPoints.of(points), at);
    }

    private LoyaltyPointsRedeemed redeemed(int points, Instant at, int burned) {
        return new LoyaltyPointsRedeemed(walletNumber, owner, owner, LoyaltyPoints.of(points), LoyaltyPoints.of(burned), at);
    }

    private RedemptionWindowReset reset(Instant at) {
        return new RedemptionWindowReset(walletNumber, owner, at);
    }

    @Test
    void aggregatesEarnsRedemptionsAndClosedWindowsForTheMonth() {
        MonthlySummary summary = fold(
                earned(100, june),
                redeemed(40, june, 38),
                redeemed(10, june, 10),
                reset(june)
        );

        assertThat(summary.month()).isEqualTo("2026-06");
        assertThat(summary.id()).isEqualTo(walletNumber.value() + ":2026-06");
        assertThat(summary.ownerId()).isEqualTo(owner);
        assertThat(summary.totalEarned()).isEqualTo(100);
        assertThat(summary.totalRedeemed()).isEqualTo(50);
        assertThat(summary.totalBurned()).isEqualTo(48);
        assertThat(summary.redemptionCount()).isEqualTo(2);
        assertThat(summary.windowsClosed()).isEqualTo(1);
    }

    @Test
    void initialisesDocumentOnRedemptionWithNoPriorEarn() {
        MonthlySummary summary = fold(redeemed(25, june, 25));

        assertThat(summary.ownerId()).isEqualTo(owner);
        assertThat(summary.totalEarned()).isEqualTo(0);
        assertThat(summary.totalRedeemed()).isEqualTo(25);
        assertThat(summary.redemptionCount()).isEqualTo(1);
    }
}
