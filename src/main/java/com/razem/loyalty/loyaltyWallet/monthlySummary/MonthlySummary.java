package com.razem.loyalty.loyaltyWallet.monthlySummary;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record MonthlySummary(
        String id,
        WalletNumber walletNumber,
        MemberId ownerId,
        String month,
        int totalEarned,
        int totalRedeemed,
        int totalBurned,
        int redemptionCount,
        int windowsClosed
) {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM").withZone(ZoneOffset.UTC);

    public static String monthOf(Instant at) {
        return MONTH_FORMATTER.format(at);
    }

    private static String documentId(WalletNumber walletNumber, Instant at) {
        return walletNumber.value() + ":" + monthOf(at);
    }

    public static MonthlySummary evolve(MonthlySummary document, LoyaltyWalletEvent event) {
        return switch (event) {
            case LoyaltyWalletEvent.LoyaltyPointsEarned earned -> {
                MonthlySummary summary = document != null ? document : new MonthlySummary(
                        documentId(earned.walletNumber(), earned.at()),
                        earned.walletNumber(),
                        earned.ownerId(),
                        monthOf(earned.at()),
                        0, 0, 0, 0, 0
                );
                yield new MonthlySummary(
                        summary.id(), summary.walletNumber(), summary.ownerId(), summary.month(),
                        summary.totalEarned() + earned.points().value(),
                        summary.totalRedeemed(), summary.totalBurned(),
                        summary.redemptionCount(), summary.windowsClosed()
                );
            }
            case LoyaltyWalletEvent.LoyaltyPointsRedeemed redeemed -> {
                MonthlySummary summary = document != null ? document : new MonthlySummary(
                        documentId(redeemed.walletNumber(), redeemed.at()),
                        redeemed.walletNumber(),
                        redeemed.ownerId(),
                        monthOf(redeemed.at()),
                        0, 0, 0, 0, 0
                );
                int burned = redeemed.burned().value();
                yield new MonthlySummary(
                        summary.id(), summary.walletNumber(), summary.ownerId(), summary.month(),
                        summary.totalEarned(),
                        summary.totalRedeemed() + redeemed.points().value(),
                        summary.totalBurned() + burned,
                        summary.redemptionCount() + 1,
                        summary.windowsClosed()
                );
            }
            case LoyaltyWalletEvent.RedemptionWindowReset reset -> {
                MonthlySummary summary = document != null ? document : new MonthlySummary(
                        documentId(reset.walletNumber(), reset.at()),
                        reset.walletNumber(),
                        reset.ownerId(),
                        monthOf(reset.at()),
                        0, 0, 0, 0, 0
                );
                yield new MonthlySummary(
                        summary.id(), summary.walletNumber(), summary.ownerId(), summary.month(),
                        summary.totalEarned(), summary.totalRedeemed(), summary.totalBurned(),
                        summary.redemptionCount(), summary.windowsClosed() + 1
                );
            }
            default -> document;
        };
    }
}
