package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;

public class LoyaltyWalletDecider {

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> decide(
            LoyaltyWalletCommand command,
            LoyaltyWallet wallet
    ) {
        return switch (command) {
            case OpenLoyaltyWallet cmd -> openLoyaltyWallet(cmd, wallet);
            case EarnLoyaltyPoints cmd -> earnLoyaltyPoints(cmd, wallet);
            case RedeemLoyaltyPoints cmd -> redeemLoyaltyPoints(cmd, wallet);
            case SetRedemptionCadence cmd -> setRedemptionCadence(cmd, wallet);
            case GrantWalletAccess cmd -> grantWalletAccess(cmd, wallet);
            case RevokeWalletAccess cmd -> revokeWalletAccess(cmd, wallet);
            case ResetRedemptionWindow cmd -> resetRedemptionWindow(cmd, wallet);
            case DeactivateWallet cmd -> deactivateWallet(cmd, wallet);
            case CloseWallet cmd -> closeWallet(cmd, wallet);
        };
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> openLoyaltyWallet(
            OpenLoyaltyWallet command,
            LoyaltyWallet wallet
    ) {
        if (!(wallet instanceof LoyaltyWallet.NotExisting)) {
            return DecideResult.of(wallet);
        }

        LoyaltyWallet.Active active = LoyaltyWallet.open(
                command.walletNumber(),
                command.ownerId(),
                command.cadence(),
                command.maxRedemptionCount(),
                command.earnedPoints(),
                command.redeemedPoints()
        );

        return DecideResult.of(active,
                new LoyaltyWalletOpened(active.walletNumber(), active.ownerId()));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> earnLoyaltyPoints(
            EarnLoyaltyPoints command,
            LoyaltyWallet wallet
    ) {
        LoyaltyWallet.Active active = assertActive(wallet);

        LoyaltyWallet.Active updated = new LoyaltyWallet.Active(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit().earn(command.points()),
                active.cadence(),
                active.access()
        );

        return DecideResult.of(updated,
                new LoyaltyPointsEarned(
                        active.walletNumber(),
                        active.ownerId(),
                        command.points(),
                        command.at()
                ));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> redeemLoyaltyPoints(
            RedeemLoyaltyPoints command,
            LoyaltyWallet wallet
    ) {
        LoyaltyWallet.Active active = assertActive(wallet);

        if (!active.access().hasAccess(command.memberId())) {
            throw new IllegalStateException("Not authorized to redeem");
        }

        if (active.pointsLimit().redemptionsLeft() <= 0) {
            throw new IllegalStateException("Redemption window exhausted");
        }

        LoyaltyPoints burned = command.burned();
        if (active.pointsLimit().availablePoints().value() < burned.value()) {
            throw new IllegalStateException("Not enough points to redeem");
        }

        LoyaltyWallet.Active updated = new LoyaltyWallet.Active(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit().redeem(command.points(), burned),
                active.cadence(),
                active.access()
        );

        return DecideResult.of(updated,
                new LoyaltyPointsRedeemed(
                        active.walletNumber(),
                        active.ownerId(),
                        command.memberId(),
                        command.points(),
                        burned,
                        command.at()
                ));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> setRedemptionCadence(
            SetRedemptionCadence command,
            LoyaltyWallet wallet
    ) {
        LoyaltyWallet.Active active = assertActive(wallet);

        LoyaltyWallet.Active updated = new LoyaltyWallet.Active(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit(),
                command.cadence(),
                active.access()
        );

        return DecideResult.of(updated,
                new RedemptionCadenceSet(
                        active.walletNumber(),
                        active.ownerId(),
                        command.cadence()
                ));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantWalletAccess(
            GrantWalletAccess command,
            LoyaltyWallet wallet
    ) {
        LoyaltyWallet.Active active = assertActive(wallet);

        LoyaltyWallet.Active updated = new LoyaltyWallet.Active(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit(),
                active.cadence(),
                active.access().grant(command.memberId())
        );

        return DecideResult.of(updated,
                new WalletAccessGranted(
                        active.walletNumber(),
                        active.ownerId(),
                        command.memberId()
                ));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> revokeWalletAccess(
            RevokeWalletAccess command,
            LoyaltyWallet wallet
    ) {
        LoyaltyWallet.Active active = assertActive(wallet);

        LoyaltyWallet.Active updated = new LoyaltyWallet.Active(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit(),
                active.cadence(),
                active.access().revoke(command.memberId())
        );

        return DecideResult.of(updated,
                new WalletAccessRevoked(
                        active.walletNumber(),
                        active.ownerId(),
                        command.memberId()
                ));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> resetRedemptionWindow(
            ResetRedemptionWindow command,
            LoyaltyWallet wallet
    ) {
        LoyaltyWallet.Active active = assertActive(wallet);

        LoyaltyWallet.Active updated = new LoyaltyWallet.Active(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit().resetRedemptionCount(),
                active.cadence(),
                active.access()
        );

        return DecideResult.of(updated,
                new RedemptionWindowReset(
                        active.walletNumber(),
                        active.ownerId(),
                        command.at()
                ));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> deactivateWallet(
            DeactivateWallet command,
            LoyaltyWallet wallet
    ) {
        if (wallet instanceof LoyaltyWallet.Deactivated deactivated) {
            return DecideResult.of(deactivated);
        }

        LoyaltyWallet.Active active = assertActive(wallet);

        LoyaltyWallet.Deactivated deactivated = new LoyaltyWallet.Deactivated(
                active.walletNumber(),
                active.ownerId(),
                active.pointsLimit(),
                active.cadence(),
                active.access()
        );

        return DecideResult.of(deactivated,
                new WalletDeactivated(active.walletNumber(), active.ownerId()));
    }

    public static DecideResult<LoyaltyWallet, LoyaltyWalletEvent> closeWallet(
            CloseWallet command,
            LoyaltyWallet wallet
    ) {
        if (wallet instanceof LoyaltyWallet.Closed closed) {
            return DecideResult.of(closed);
        }

        if (wallet instanceof LoyaltyWallet.NotExisting) {
            throw new IllegalStateException("Wallet doesn't exist");
        }

        WalletNumber walletNumber = switch (wallet) {
            case LoyaltyWallet.Active active -> active.walletNumber();
            case LoyaltyWallet.Deactivated deactivated -> deactivated.walletNumber();
            default -> throw new IllegalStateException("Unexpected state");
        };

        LoyaltyWallet.Closed closed = new LoyaltyWallet.Closed(walletNumber);
        return DecideResult.of(closed, new WalletClosed(walletNumber));
    }

    private static LoyaltyWallet.Active assertActive(LoyaltyWallet wallet) {
        return switch (wallet) {
            case LoyaltyWallet.Active active -> active;
            case LoyaltyWallet.NotExisting n -> throw new IllegalStateException("Wallet doesn't exist");
            case LoyaltyWallet.Deactivated d -> throw new IllegalStateException("Wallet is not active");
            case LoyaltyWallet.Closed c -> throw new IllegalStateException("Wallet is closed");
        };
    }
}
