package com.razem.loyalty.loyaltyWallet.activityReport;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class ActivityReportTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    private ActivityReport fold(LoyaltyWalletEvent... events) {
        ActivityReport result = null;
        for (LoyaltyWalletEvent event : events) {
            result = ActivityReport.evolve(result, event);
        }
        return result;
    }

    private LoyaltyWalletOpened opened() {
        return new LoyaltyWalletOpened(walletNumber, owner);
    }

    private LoyaltyPointsEarned earned(int points) {
        return new LoyaltyPointsEarned(walletNumber, owner, LoyaltyPoints.of(points), at);
    }

    private LoyaltyPointsRedeemed redeemed(int points, int burned) {
        return new LoyaltyPointsRedeemed(walletNumber, owner, owner, LoyaltyPoints.of(points), LoyaltyPoints.of(burned), at);
    }

    private RedemptionWindowReset reset() {
        return new RedemptionWindowReset(walletNumber, owner, at);
    }

    @Test
    void opensAnEmptyReportInItsFirstWindow() {
        ActivityReport report = fold(opened());

        assertThat(report).isNotNull();
        assertThat(report.walletNumber()).isEqualTo(walletNumber);
        assertThat(report.ownerId()).isEqualTo(owner);
        assertThat(report.currentWindow().windowNumber()).isEqualTo(1);
        assertThat(report.currentWindow().hadActivity()).isFalse();
        assertThat(report.closedWindows()).isEmpty();
    }

    @Test
    void groupsEarnsAndRedemptionsIntoCurrentWindow() {
        ActivityReport report = fold(opened(), earned(100), redeemed(40, 38));

        assertThat(report.currentWindow().earned()).isEqualTo(100);
        assertThat(report.currentWindow().redeemed()).isEqualTo(40);
        assertThat(report.currentWindow().burned()).isEqualTo(38);
        assertThat(report.currentWindow().redemptionCount()).isEqualTo(1);
        assertThat(report.currentWindow().hadActivity()).isTrue();
        assertThat(report.currentWindow().entries()).hasSize(2);
        assertThat(report.currentWindow().entries().get(0).kind()).isEqualTo(ActivityEntry.Kind.EARNED);
        assertThat(report.currentWindow().entries().get(0).points()).isEqualTo(100);
        assertThat(report.currentWindow().entries().get(1).kind()).isEqualTo(ActivityEntry.Kind.REDEEMED);
        assertThat(report.currentWindow().entries().get(1).points()).isEqualTo(40);
    }

    @Test
    void closesCurrentWindowAndOpensNextOnReset() {
        ActivityReport report = fold(opened(), earned(100), redeemed(40, 38), reset(), earned(20));

        assertThat(report.closedWindows()).hasSize(1);
        assertThat(report.closedWindows().get(0).windowNumber()).isEqualTo(1);
        assertThat(report.closedWindows().get(0).earned()).isEqualTo(100);
        assertThat(report.closedWindows().get(0).redeemed()).isEqualTo(40);
        assertThat(report.closedWindows().get(0).burned()).isEqualTo(38);
        assertThat(report.currentWindow().windowNumber()).isEqualTo(2);
        assertThat(report.currentWindow().earned()).isEqualTo(20);
        assertThat(report.currentWindow().redeemed()).isEqualTo(0);
    }

    @Test
    void ignoresActivityBeforeReportIsOpened() {
        ActivityReport report = fold(earned(100));

        assertThat(report).isNull();
    }
}
