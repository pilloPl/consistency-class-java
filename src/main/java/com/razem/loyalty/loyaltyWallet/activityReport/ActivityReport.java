package com.razem.loyalty.loyaltyWallet.activityReport;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.util.ArrayList;
import java.util.List;

public record ActivityReport(
        String id,
        WalletNumber walletNumber,
        MemberId ownerId,
        WindowActivity currentWindow,
        List<WindowActivity> closedWindows
) {
    public static ActivityReport evolve(ActivityReport document, LoyaltyWalletEvent event) {
        return switch (event) {
            case LoyaltyWalletEvent.LoyaltyWalletOpened opened -> {
                yield new ActivityReport(
                        opened.walletNumber().value(),
                        opened.walletNumber(),
                        opened.ownerId(),
                        WindowActivity.empty(1),
                        List.of()
                );
            }
            case LoyaltyWalletEvent.LoyaltyPointsEarned earned -> {
                if (document == null) yield null;
                WindowActivity window = document.currentWindow();
                List<ActivityEntry> newEntries = new ArrayList<>(window.entries());
                newEntries.add(new ActivityEntry(ActivityEntry.Kind.EARNED, earned.points().value(), earned.at()));
                WindowActivity newWindow = new WindowActivity(
                        window.windowNumber(),
                        window.earned() + earned.points().value(),
                        window.redeemed(),
                        window.burned(),
                        window.redemptionCount(),
                        true,
                        List.copyOf(newEntries)
                );
                yield new ActivityReport(
                        document.id(),
                        document.walletNumber(),
                        document.ownerId(),
                        newWindow,
                        document.closedWindows()
                );
            }
            case LoyaltyWalletEvent.LoyaltyPointsRedeemed redeemed -> {
                if (document == null) yield null;
                WindowActivity window = document.currentWindow();
                int burned = redeemed.burned().value();
                List<ActivityEntry> newEntries = new ArrayList<>(window.entries());
                newEntries.add(new ActivityEntry(ActivityEntry.Kind.REDEEMED, redeemed.points().value(), redeemed.at()));
                WindowActivity newWindow = new WindowActivity(
                        window.windowNumber(),
                        window.earned(),
                        window.redeemed() + redeemed.points().value(),
                        window.burned() + burned,
                        window.redemptionCount() + 1,
                        true,
                        List.copyOf(newEntries)
                );
                yield new ActivityReport(
                        document.id(),
                        document.walletNumber(),
                        document.ownerId(),
                        newWindow,
                        document.closedWindows()
                );
            }
            case LoyaltyWalletEvent.RedemptionWindowReset reset -> {
                if (document == null) yield null;
                List<WindowActivity> newClosedWindows = new ArrayList<>(document.closedWindows());
                newClosedWindows.add(document.currentWindow());
                WindowActivity newWindow = WindowActivity.empty(document.currentWindow().windowNumber() + 1);
                yield new ActivityReport(
                        document.id(),
                        document.walletNumber(),
                        document.ownerId(),
                        newWindow,
                        List.copyOf(newClosedWindows)
                );
            }
            default -> document;
        };
    }
}
