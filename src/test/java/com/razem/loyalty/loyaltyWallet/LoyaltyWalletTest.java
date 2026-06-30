package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.*;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class LoyaltyWalletTest {

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final MemberId familyMember = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    private LoyaltyWallet openWallet() {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        return wallet;
    }

    private LoyaltyWallet walletWithPoints(int points) {
        LoyaltyWallet wallet = openWallet();
        wallet.earnPoints(LoyaltyPoints.of(points), at);
        return wallet;
    }

    private LoyaltyWallet deactivated() {
        LoyaltyWallet wallet = openWallet();
        wallet.deactivate();
        return wallet;
    }

    private LoyaltyWallet closed() {
        LoyaltyWallet wallet = openWallet();
        wallet.close();
        return wallet;
    }

    @Nested
    class Opening {
        @Test
        void opensNotExistingWalletAndEmitsEvent() {
            LoyaltyWallet wallet = LoyaltyWallet.notExisting();

            List<LoyaltyWalletEvent> events = wallet.open(
                    walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
            );

            assertThat(wallet.isActive()).isTrue();
            assertThat(wallet.walletNumber()).isEqualTo(walletNumber);
            assertThat(wallet.ownerId()).isEqualTo(owner);
            assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(0);
            assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(5);
            assertThat(wallet.access().hasAccess(owner)).isTrue();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(new LoyaltyWalletOpened(walletNumber, owner));
        }

        @Test
        void leavesAlreadyActiveWalletUnchangedWithoutEvents() {
            LoyaltyWallet wallet = openWallet();

            List<LoyaltyWalletEvent> events = wallet.open(
                    walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)
            );

            assertThat(events).isEmpty();
            assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
        }

        @Test
        void leavesDeactivatedWalletUnchangedWithoutEvents() {
            LoyaltyWallet wallet = deactivated();

            List<LoyaltyWalletEvent> events = wallet.open(
                    walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
            );

            assertThat(events).isEmpty();
            assertThat(wallet.isDeactivated()).isTrue();
        }

        @Test
        void leavesClosedWalletUnchangedWithoutEvents() {
            LoyaltyWallet wallet = closed();

            List<LoyaltyWalletEvent> events = wallet.open(
                    walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)
            );

            assertThat(events).isEmpty();
            assertThat(wallet.isClosed()).isTrue();
        }
    }

    @Nested
    class EarningPoints {
        @Test
        void earnsPointsOnActiveWalletAndEmitsEvent() {
            LoyaltyWallet wallet = openWallet();

            List<LoyaltyWalletEvent> events = wallet.earnPoints(LoyaltyPoints.of(100), at);

            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(100);
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(
                    new LoyaltyPointsEarned(walletNumber, owner, LoyaltyPoints.of(100), at)
            );
        }

        @Test
        void cannotEarnOnNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWallet.notExisting().earnPoints(LoyaltyPoints.of(100), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }

        @Test
        void cannotEarnOnDeactivatedWallet() {
            assertThatThrownBy(() ->
                    deactivated().earnPoints(LoyaltyPoints.of(100), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }

        @Test
        void cannotEarnOnClosedWallet() {
            assertThatThrownBy(() ->
                    closed().earnPoints(LoyaltyPoints.of(100), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
        }
    }

    @Nested
    class RedeemingPoints {
        @Test
        void redeemsPointsOnActiveWalletAndEmitsEvent() {
            LoyaltyWallet wallet = walletWithPoints(100);

            List<LoyaltyWalletEvent> events = wallet.redeemPoints(owner, LoyaltyPoints.of(40), at);

            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
            assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(4);
            assertThat(events).hasSize(1);
            LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) events.get(0);
            assertThat(event.walletNumber()).isEqualTo(walletNumber);
            assertThat(event.ownerId()).isEqualTo(owner);
            assertThat(event.byMemberId()).isEqualTo(owner);
            assertThat(event.points().value()).isEqualTo(40);
            assertThat(event.burned().value()).isEqualTo(40);
        }

        @Test
        void burnsPolicyAmountFromBalanceWhileRecordingRedeemedAmount() {
            LoyaltyWallet wallet = walletWithPoints(100);

            List<LoyaltyWalletEvent> events = wallet.redeemPoints(
                    owner, LoyaltyPoints.of(100), LoyaltyPoints.of(95), at
            );

            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(5);
            LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) events.get(0);
            assertThat(event.points().value()).isEqualTo(100);
            assertThat(event.burned().value()).isEqualTo(95);
        }

        @Test
        void cannotRedeemMoreThanAvailable() {
            assertThatThrownBy(() ->
                    walletWithPoints(20).redeemPoints(owner, LoyaltyPoints.of(50), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Not enough points to redeem");
        }

        @Test
        void cannotRedeemOnNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWallet.notExisting().redeemPoints(owner, LoyaltyPoints.of(40), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }

        @Test
        void cannotRedeemOnDeactivatedWallet() {
            assertThatThrownBy(() ->
                    deactivated().redeemPoints(owner, LoyaltyPoints.of(40), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }

        @Test
        void cannotRedeemOnClosedWallet() {
            assertThatThrownBy(() ->
                    closed().redeemPoints(owner, LoyaltyPoints.of(40), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
        }
    }

    @Nested
    class SettingRedemptionCadence {
        @Test
        void changesCadenceOnActiveWalletAndEmitsEvent() {
            LoyaltyWallet wallet = openWallet();

            List<LoyaltyWalletEvent> events = wallet.setRedemptionCadence(RedemptionCadence.MONTHLY);

            assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.MONTHLY);
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(
                    new RedemptionCadenceSet(walletNumber, owner, RedemptionCadence.MONTHLY)
            );
        }

        @Test
        void cannotSetCadenceOnNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWallet.notExisting().setRedemptionCadence(RedemptionCadence.MONTHLY)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }
    }

    @Nested
    class WalletAccessTests {
        @Test
        void ownerCanRedeemFromBalance() {
            LoyaltyWallet wallet = walletWithPoints(100);
            wallet.redeemPoints(owner, LoyaltyPoints.of(40), at);
            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
        }

        @Test
        void grantsAccessToFamilyMemberAndEmitsEvent() {
            LoyaltyWallet wallet = openWallet();

            List<LoyaltyWalletEvent> events = wallet.grantAccess(familyMember);

            assertThat(wallet.access().hasAccess(familyMember)).isTrue();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(
                    new WalletAccessGranted(walletNumber, owner, familyMember)
            );
        }

        @Test
        void revokesAccessFromFamilyMemberAndEmitsEvent() {
            LoyaltyWallet wallet = openWallet();
            wallet.grantAccess(familyMember);

            List<LoyaltyWalletEvent> events = wallet.revokeAccess(familyMember);

            assertThat(wallet.access().hasAccess(familyMember)).isFalse();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(
                    new WalletAccessRevoked(walletNumber, owner, familyMember)
            );
        }

        @Test
        void grantedFamilyMemberCanRedeemFromBalance() {
            LoyaltyWallet wallet = walletWithPoints(100);
            wallet.grantAccess(familyMember);

            wallet.redeemPoints(familyMember, LoyaltyPoints.of(40), at);

            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
        }

        @Test
        void cannotRedeemWithoutAccess() {
            assertThatThrownBy(() ->
                    walletWithPoints(100).redeemPoints(familyMember, LoyaltyPoints.of(40), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
        }

        @Test
        void cannotRedeemAfterAccessRevoked() {
            LoyaltyWallet wallet = walletWithPoints(100);
            wallet.grantAccess(familyMember);
            wallet.revokeAccess(familyMember);

            assertThatThrownBy(() ->
                    wallet.redeemPoints(familyMember, LoyaltyPoints.of(40), at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
        }

        @Test
        void cannotGrantAccessIfWalletNotActive() {
            assertThatThrownBy(() ->
                    deactivated().grantAccess(familyMember)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }

        @Test
        void cannotRevokeAccessIfWalletNotActive() {
            assertThatThrownBy(() ->
                    deactivated().revokeAccess(familyMember)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }
    }

    @Nested
    class ResettingRedemptionWindow {
        @Test
        void resetsRedemptionCountKeepingBalanceAndEmitsEvent() {
            LoyaltyWallet wallet = walletWithPoints(100);
            wallet.redeemPoints(owner, LoyaltyPoints.of(10), at);
            assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(4);

            List<LoyaltyWalletEvent> events = wallet.resetRedemptionWindow(at);

            assertThat(wallet.pointsLimit().redemptionsLeft()).isEqualTo(5);
            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(90);
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(new RedemptionWindowReset(walletNumber, owner, at));
        }

        @Test
        void cannotResetWindowIfWalletNotActive() {
            assertThatThrownBy(() ->
                    deactivated().resetRedemptionWindow(at)
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }
    }

    @Nested
    class Deactivating {
        @Test
        void deactivatesActiveWalletKeepingDataAndEmitsEvent() {
            LoyaltyWallet wallet = walletWithPoints(100);
            wallet.redeemPoints(owner, LoyaltyPoints.of(10), at);

            List<LoyaltyWalletEvent> events = wallet.deactivate();

            assertThat(wallet.isDeactivated()).isTrue();
            assertThat(wallet.walletNumber()).isEqualTo(walletNumber);
            assertThat(wallet.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
            assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(90);
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(new WalletDeactivated(walletNumber, owner));
        }

        @Test
        void leavesAlreadyDeactivatedWalletUnchangedWithoutEvents() {
            LoyaltyWallet wallet = deactivated();

            List<LoyaltyWalletEvent> events = wallet.deactivate();

            assertThat(events).isEmpty();
            assertThat(wallet.isDeactivated()).isTrue();
        }

        @Test
        void cannotDeactivateNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWallet.notExisting().deactivate()
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }

        @Test
        void cannotDeactivateClosedWallet() {
            assertThatThrownBy(() ->
                    closed().deactivate()
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
        }
    }

    @Nested
    class Closing {
        @Test
        void closesActiveWalletAndEmitsEvent() {
            LoyaltyWallet wallet = openWallet();

            List<LoyaltyWalletEvent> events = wallet.close();

            assertThat(wallet.isClosed()).isTrue();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isEqualTo(new WalletClosed(walletNumber));
        }

        @Test
        void closesDeactivatedWallet() {
            LoyaltyWallet wallet = deactivated();

            List<LoyaltyWalletEvent> events = wallet.close();

            assertThat(wallet.isClosed()).isTrue();
            assertThat(events).hasSize(1);
        }

        @Test
        void leavesAlreadyClosedWalletUnchangedWithoutEvents() {
            LoyaltyWallet wallet = closed();

            List<LoyaltyWalletEvent> events = wallet.close();

            assertThat(events).isEmpty();
            assertThat(wallet.isClosed()).isTrue();
        }

        @Test
        void cannotCloseNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWallet.notExisting().close()
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }
    }
}
