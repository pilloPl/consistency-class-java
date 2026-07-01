package com.razem.loyalty.loyaltyWallet.openingWallet;

import com.razem.loyalty.loyaltyWallet.LoyaltyWallet;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletEvent;
import com.razem.loyalty.loyaltyWallet.LoyaltyWalletRepository;
import com.razem.loyalty.loyaltyWallet.RedemptionCadence;
import com.razem.loyalty.loyaltyWallet.RedemptionLimit;
import com.razem.loyalty.loyaltyWallet.WalletAccess;
import com.razem.loyalty.loyaltyWallet.WalletAccessRepository;
import com.razem.loyalty.loyaltyWallet.WalletNumber;
import com.razem.loyalty.loyaltyWallet.access.MemberId;

import java.util.List;

public class OpenLoyaltyWalletService {

    private final LoyaltyWalletRepository walletRepository;
    private final WalletAccessRepository accessRepository;

    public OpenLoyaltyWalletService(
            LoyaltyWalletRepository walletRepository,
            WalletAccessRepository accessRepository
    ) {
        this.walletRepository = walletRepository;
        this.accessRepository = accessRepository;
    }

    public LoyaltyWallet open(
            WalletNumber walletNumber,
            MemberId ownerId,
            RedemptionCadence cadence,
            RedemptionLimit maxRedemptionCount
    ) {
        LoyaltyWallet wallet = walletRepository.findByWalletNumber(walletNumber)
                .orElse(LoyaltyWallet.notExisting());

        List<LoyaltyWalletEvent> walletEvents = wallet.open(walletNumber, ownerId, cadence, maxRedemptionCount);
        walletRepository.save(walletNumber, wallet, walletEvents);

        if (!walletEvents.isEmpty()) {
            WalletAccess access = accessRepository.findByWalletNumber(walletNumber)
                    .orElse(WalletAccess.notExisting());
            List<LoyaltyWalletEvent> accessEvents = access.initialize(walletNumber, ownerId);
            accessRepository.save(walletNumber, access, accessEvents);
        }

        return wallet;
    }
}
