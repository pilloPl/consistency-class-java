package com.razem.loyalty.loyaltyWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWalletCommand.*;
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

    private LoyaltyWallet.Active openWallet() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                LoyaltyWallet.initial()
        );
        return (LoyaltyWallet.Active) result.state();
    }

    private LoyaltyWallet.Active walletWithPoints(int points) {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.earnLoyaltyPoints(
                new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(points), at),
                openWallet()
        );
        return (LoyaltyWallet.Active) result.state();
    }

    private LoyaltyWallet.Deactivated deactivated() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                new DeactivateWallet(walletNumber),
                openWallet()
        );
        return (LoyaltyWallet.Deactivated) result.state();
    }

    private LoyaltyWallet.Closed closed() {
        DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                new CloseWallet(walletNumber),
                openWallet()
        );
        return (LoyaltyWallet.Closed) result.state();
    }

    @Nested
    class Opening {
        @Test
        void opensNotExistingWalletAndEmitsEvent() {
            LoyaltyWallet initial = LoyaltyWallet.initial();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                    new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                    initial
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.walletNumber()).isEqualTo(walletNumber);
            assertThat(active.ownerId()).isEqualTo(owner);
            assertThat(active.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(0);
            assertThat(active.pointsLimit().redemptionsLeft()).isEqualTo(5);
            assertThat(active.access().hasAccess(owner)).isTrue();
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(new LoyaltyWalletOpened(walletNumber, owner));
        }

        @Test
        void leavesAlreadyActiveWalletUnchangedWithoutEvents() {
            LoyaltyWallet.Active activeWallet = openWallet();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                    new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.MONTHLY, RedemptionLimit.of(3)),
                    activeWallet
            );

            assertThat(result.state()).isSameAs(activeWallet);
            assertThat(result.events()).isEmpty();
        }

        @Test
        void leavesDeactivatedWalletUnchangedWithoutEvents() {
            LoyaltyWallet.Deactivated deactivatedWallet = deactivated();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                    new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                    deactivatedWallet
            );

            assertThat(result.state()).isSameAs(deactivatedWallet);
            assertThat(result.events()).isEmpty();
        }

        @Test
        void leavesClosedWalletUnchangedWithoutEvents() {
            LoyaltyWallet.Closed closedWallet = closed();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.openLoyaltyWallet(
                    new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                    closedWallet
            );

            assertThat(result.state()).isSameAs(closedWallet);
            assertThat(result.events()).isEmpty();
        }
    }

    @Nested
    class EarningPoints {
        @Test
        void earnsPointsOnActiveWalletAndEmitsEvent() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.earnLoyaltyPoints(
                    new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                    openWallet()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(100);
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(
                    new LoyaltyPointsEarned(walletNumber, owner, LoyaltyPoints.of(100), at)
            );
        }

        @Test
        void cannotEarnOnNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.earnLoyaltyPoints(
                            new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                            LoyaltyWallet.initial()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }

        @Test
        void cannotEarnOnDeactivatedWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.earnLoyaltyPoints(
                            new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                            deactivated()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }

        @Test
        void cannotEarnOnClosedWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.earnLoyaltyPoints(
                            new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                            closed()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
        }
    }

    @Nested
    class RedeemingPoints {
        @Test
        void redeemsPointsOnActiveWalletAndEmitsEvent() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                    new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                    walletWithPoints(100)
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
            assertThat(active.pointsLimit().redemptionsLeft()).isEqualTo(4);
            assertThat(result.events()).hasSize(1);
            LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) result.events().get(0);
            assertThat(event.walletNumber()).isEqualTo(walletNumber);
            assertThat(event.ownerId()).isEqualTo(owner);
            assertThat(event.byMemberId()).isEqualTo(owner);
            assertThat(event.points().value()).isEqualTo(40);
            assertThat(event.burned().value()).isEqualTo(40);
        }

        @Test
        void burnsPolicyAmountFromBalanceWhileRecordingRedeemedAmount() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                    new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(100), LoyaltyPoints.of(95), at),
                    walletWithPoints(100)
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(5);
            LoyaltyPointsRedeemed event = (LoyaltyPointsRedeemed) result.events().get(0);
            assertThat(event.points().value()).isEqualTo(100);
            assertThat(event.burned().value()).isEqualTo(95);
        }

        @Test
        void cannotRedeemMoreThanAvailable() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.redeemLoyaltyPoints(
                            new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(50), at),
                            walletWithPoints(20)
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Not enough points to redeem");
        }

        @Test
        void cannotRedeemOnNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.redeemLoyaltyPoints(
                            new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                            LoyaltyWallet.initial()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }

        @Test
        void cannotRedeemOnDeactivatedWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.redeemLoyaltyPoints(
                            new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                            deactivated()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }

        @Test
        void cannotRedeemOnClosedWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.redeemLoyaltyPoints(
                            new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                            closed()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
        }
    }

    @Nested
    class SettingRedemptionCadence {
        @Test
        void changesCadenceOnActiveWalletAndEmitsEvent() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.setRedemptionCadence(
                    new SetRedemptionCadence(walletNumber, RedemptionCadence.MONTHLY),
                    openWallet()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.cadence()).isEqualTo(RedemptionCadence.MONTHLY);
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(
                    new RedemptionCadenceSet(walletNumber, owner, RedemptionCadence.MONTHLY)
            );
        }

        @Test
        void cannotSetCadenceOnNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.setRedemptionCadence(
                            new SetRedemptionCadence(walletNumber, RedemptionCadence.MONTHLY),
                            LoyaltyWallet.initial()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }
    }

    @Nested
    class WalletAccessTests {
        @Test
        void ownerCanRedeemFromBalance() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.redeemLoyaltyPoints(
                    new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                    walletWithPoints(100)
            );
            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
        }

        @Test
        void grantsAccessToFamilyMemberAndEmitsEvent() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.grantWalletAccess(
                    new GrantWalletAccess(walletNumber, familyMember),
                    openWallet()
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.access().hasAccess(familyMember)).isTrue();
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(
                    new WalletAccessGranted(walletNumber, owner, familyMember)
            );
        }

        @Test
        void revokesAccessFromFamilyMemberAndEmitsEvent() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantResult = LoyaltyWalletDecider.grantWalletAccess(
                    new GrantWalletAccess(walletNumber, familyMember),
                    openWallet()
            );
            LoyaltyWallet.Active shared = (LoyaltyWallet.Active) grantResult.state();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> revokeResult = LoyaltyWalletDecider.revokeWalletAccess(
                    new RevokeWalletAccess(walletNumber, familyMember),
                    shared
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) revokeResult.state();
            assertThat(active.access().hasAccess(familyMember)).isFalse();
            assertThat(revokeResult.events()).hasSize(1);
            assertThat(revokeResult.events().get(0)).isEqualTo(
                    new WalletAccessRevoked(walletNumber, owner, familyMember)
            );
        }

        @Test
        void grantedFamilyMemberCanRedeemFromBalance() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantResult = LoyaltyWalletDecider.grantWalletAccess(
                    new GrantWalletAccess(walletNumber, familyMember),
                    walletWithPoints(100)
            );
            LoyaltyWallet.Active shared = (LoyaltyWallet.Active) grantResult.state();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> redeemResult = LoyaltyWalletDecider.redeemLoyaltyPoints(
                    new RedeemLoyaltyPoints(walletNumber, familyMember, LoyaltyPoints.of(40), at),
                    shared
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) redeemResult.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
        }

        @Test
        void cannotRedeemWithoutAccess() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.redeemLoyaltyPoints(
                            new RedeemLoyaltyPoints(walletNumber, familyMember, LoyaltyPoints.of(40), at),
                            walletWithPoints(100)
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
        }

        @Test
        void cannotRedeemAfterAccessRevoked() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> grantResult = LoyaltyWalletDecider.grantWalletAccess(
                    new GrantWalletAccess(walletNumber, familyMember),
                    walletWithPoints(100)
            );
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> revokeResult = LoyaltyWalletDecider.revokeWalletAccess(
                    new RevokeWalletAccess(walletNumber, familyMember),
                    (LoyaltyWallet.Active) grantResult.state()
            );

            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.redeemLoyaltyPoints(
                            new RedeemLoyaltyPoints(walletNumber, familyMember, LoyaltyPoints.of(40), at),
                            revokeResult.state()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
        }

        @Test
        void cannotGrantAccessIfWalletNotActive() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.grantWalletAccess(
                            new GrantWalletAccess(walletNumber, familyMember),
                            deactivated()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }

        @Test
        void cannotRevokeAccessIfWalletNotActive() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.revokeWalletAccess(
                            new RevokeWalletAccess(walletNumber, familyMember),
                            deactivated()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }
    }

    @Nested
    class ResettingRedemptionWindow {
        @Test
        void resetsRedemptionCountKeepingBalanceAndEmitsEvent() {
            LoyaltyWallet.Active walletAfterRedeem = (LoyaltyWallet.Active) LoyaltyWalletDecider.redeemLoyaltyPoints(
                    new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(10), at),
                    walletWithPoints(100)
            ).state();
            assertThat(walletAfterRedeem.pointsLimit().redemptionsLeft()).isEqualTo(4);

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.resetRedemptionWindow(
                    new ResetRedemptionWindow(walletNumber, at),
                    walletAfterRedeem
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().redemptionsLeft()).isEqualTo(5);
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(90);
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(
                    new RedemptionWindowReset(walletNumber, owner, at)
            );
        }

        @Test
        void cannotResetWindowIfWalletNotActive() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.resetRedemptionWindow(
                            new ResetRedemptionWindow(walletNumber, at),
                            deactivated()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is not active");
        }
    }

    @Nested
    class Deactivating {
        @Test
        void deactivatesActiveWalletKeepingDataAndEmitsEvent() {
            LoyaltyWallet.Active walletAfterRedeem = (LoyaltyWallet.Active) LoyaltyWalletDecider.redeemLoyaltyPoints(
                    new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(10), at),
                    walletWithPoints(100)
            ).state();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                    new DeactivateWallet(walletNumber),
                    walletAfterRedeem
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Deactivated.class);
            LoyaltyWallet.Deactivated deactivated = (LoyaltyWallet.Deactivated) result.state();
            assertThat(deactivated.walletNumber()).isEqualTo(walletNumber);
            assertThat(deactivated.cadence()).isEqualTo(RedemptionCadence.WEEKLY);
            assertThat(deactivated.pointsLimit().availablePoints().value()).isEqualTo(90);
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(new WalletDeactivated(walletNumber, owner));
        }

        @Test
        void leavesAlreadyDeactivatedWalletUnchangedWithoutEvents() {
            LoyaltyWallet.Deactivated deactivatedWallet = deactivated();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.deactivateWallet(
                    new DeactivateWallet(walletNumber),
                    deactivatedWallet
            );

            assertThat(result.state()).isSameAs(deactivatedWallet);
            assertThat(result.events()).isEmpty();
        }

        @Test
        void cannotDeactivateNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.deactivateWallet(
                            new DeactivateWallet(walletNumber),
                            LoyaltyWallet.initial()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }

        @Test
        void cannotDeactivateClosedWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.deactivateWallet(
                            new DeactivateWallet(walletNumber),
                            closed()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet is closed");
        }
    }

    @Nested
    class Closing {
        @Test
        void closesActiveWalletAndEmitsEvent() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                    new CloseWallet(walletNumber),
                    openWallet()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Closed.class);
            assertThat(result.events()).hasSize(1);
            assertThat(result.events().get(0)).isEqualTo(new WalletClosed(walletNumber));
        }

        @Test
        void closesDeactivatedWallet() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                    new CloseWallet(walletNumber),
                    deactivated()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Closed.class);
        }

        @Test
        void leavesAlreadyClosedWalletUnchangedWithoutEvents() {
            LoyaltyWallet.Closed closedWallet = closed();

            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.closeWallet(
                    new CloseWallet(walletNumber),
                    closedWallet
            );

            assertThat(result.state()).isSameAs(closedWallet);
            assertThat(result.events()).isEmpty();
        }

        @Test
        void cannotCloseNotExistingWallet() {
            assertThatThrownBy(() ->
                    LoyaltyWalletDecider.closeWallet(
                            new CloseWallet(walletNumber),
                            LoyaltyWallet.initial()
                    )
            ).isInstanceOf(IllegalStateException.class).hasMessage("Wallet doesn't exist");
        }
    }

    @Nested
    class DecideRouting {
        @Test
        void routesOpenLoyaltyWallet() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                    new OpenLoyaltyWallet(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5)),
                    LoyaltyWallet.initial()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Active.class);
            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.walletNumber()).isEqualTo(walletNumber);
        }

        @Test
        void routesEarnLoyaltyPoints() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                    new EarnLoyaltyPoints(walletNumber, LoyaltyPoints.of(100), at),
                    openWallet()
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(100);
        }

        @Test
        void routesRedeemLoyaltyPoints() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                    new RedeemLoyaltyPoints(walletNumber, owner, LoyaltyPoints.of(40), at),
                    walletWithPoints(100)
            );

            LoyaltyWallet.Active active = (LoyaltyWallet.Active) result.state();
            assertThat(active.pointsLimit().availablePoints().value()).isEqualTo(60);
        }

        @Test
        void routesDeactivateWallet() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                    new DeactivateWallet(walletNumber),
                    openWallet()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Deactivated.class);
        }

        @Test
        void routesCloseWallet() {
            DecideResult<LoyaltyWallet, LoyaltyWalletEvent> result = LoyaltyWalletDecider.decide(
                    new CloseWallet(walletNumber),
                    openWallet()
            );

            assertThat(result.state()).isInstanceOf(LoyaltyWallet.Closed.class);
        }
    }
}
