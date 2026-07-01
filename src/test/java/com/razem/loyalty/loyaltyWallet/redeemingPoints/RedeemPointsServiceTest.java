package com.razem.loyalty.loyaltyWallet.redeemingPoints;

import com.razem.loyalty.loyaltyWallet.InMemoryLoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.InMemoryWalletAccessRepository;
import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent.LoyaltyPointsRedeemed;
import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import com.razem.loyalty.loyaltyWallet.RedemptionLimit;
import com.razem.loyalty.loyaltyWallet.WalletAccess;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class RedeemPointsServiceTest {

    private final InMemoryLoyaltyWalletRepository walletRepository = new InMemoryLoyaltyWalletRepository();
    private final InMemoryWalletAccessRepository accessRepository = new InMemoryWalletAccessRepository();
    private final RedeemPointsService service = new RedeemPointsService(walletRepository, accessRepository);

    private final WalletNumber walletNumber = WalletNumber.random();
    private final MemberId owner = MemberId.random();
    private final MemberId familyMember = MemberId.random();
    private final Instant at = Instant.parse("2026-06-23T12:00:00Z");

    @Test
    void ownerCanRedeem() {
        setupWalletWithPoints(100);

        service.redeem(walletNumber, owner, LoyaltyPoints.of(40), LoyaltyPoints.of(40), at);

        LoyaltyWallet wallet = walletRepository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
        assertThat(walletRepository.getEvents(walletNumber).getLast()).isInstanceOf(LoyaltyPointsRedeemed.class);
    }

    @Test
    void grantedMemberCanRedeem() {
        setupWalletWithPoints(100);
        WalletAccess access = accessRepository.findByWalletNumber(walletNumber).orElseThrow();
        access.grant(familyMember);
        accessRepository.save(walletNumber, access, access.grant(familyMember));

        service.redeem(walletNumber, familyMember, LoyaltyPoints.of(40), LoyaltyPoints.of(40), at);

        LoyaltyWallet wallet = walletRepository.findByWalletNumber(walletNumber).orElseThrow();
        assertThat(wallet.pointsLimit().availablePoints().value()).isEqualTo(60);
    }

    @Test
    void cannotRedeemWithoutAccess() {
        setupWalletWithPoints(100);

        assertThatThrownBy(() ->
                service.redeem(walletNumber, familyMember, LoyaltyPoints.of(40), LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
    }

    @Test
    void cannotRedeemAfterAccessRevoked() {
        setupWalletWithPoints(100);
        WalletAccess access = accessRepository.findByWalletNumber(walletNumber).orElseThrow();
        access.grant(familyMember);
        access.revoke(familyMember);

        assertThatThrownBy(() ->
                service.redeem(walletNumber, familyMember, LoyaltyPoints.of(40), LoyaltyPoints.of(40), at)
        ).isInstanceOf(IllegalStateException.class).hasMessage("Not authorized to redeem");
    }

    private void setupWalletWithPoints(int points) {
        LoyaltyWallet wallet = LoyaltyWallet.notExisting();
        wallet.open(walletNumber, owner, RedemptionCadence.WEEKLY, RedemptionLimit.of(5));
        walletRepository.save(walletNumber, wallet, wallet.earnPoints(LoyaltyPoints.of(points), at));

        WalletAccess access = WalletAccess.notExisting();
        accessRepository.save(walletNumber, access, access.initialize(walletNumber, owner));
    }
}
