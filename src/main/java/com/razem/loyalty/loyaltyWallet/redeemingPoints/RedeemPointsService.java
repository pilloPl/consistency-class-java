package com.razem.loyalty.loyaltyWallet.redeemingPoints;

import com.razem.loyalty.loyaltyWallet.LoyaltyPoints;
import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.WalletAccess;
import com.razem.loyalty.loyaltyWallet.WalletAccessRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.time.Instant;
import java.util.List;

public class RedeemPointsService {

    private final LoyaltyWalletRepository walletRepository;
    private final WalletAccessRepository accessRepository;

    public RedeemPointsService(
            LoyaltyWalletRepository walletRepository,
            WalletAccessRepository accessRepository
    ) {
        this.walletRepository = walletRepository;
        this.accessRepository = accessRepository;
    }

    public void redeem(
            WalletNumber walletNumber,
            MemberId memberId,
            LoyaltyPoints points,
            LoyaltyPoints burned,
            Instant at
    ) {
        WalletAccess access = accessRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new IllegalStateException("Wallet doesn't exist"));

        if (!access.hasAccess(memberId)) {
            throw new IllegalStateException("Not authorized to redeem");
        }

        LoyaltyWallet wallet = walletRepository.findByWalletNumber(walletNumber)
                .orElseThrow(() -> new IllegalStateException("Wallet doesn't exist"));

        List<LoyaltyWalletEvent> events = wallet.redeemPoints(memberId, points, burned, at);
        walletRepository.save(walletNumber, wallet, events);
    }
}
