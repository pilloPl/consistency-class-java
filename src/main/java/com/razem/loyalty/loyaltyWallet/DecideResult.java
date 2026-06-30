package com.razem.loyalty.loyaltyWallet;

import java.util.List;

public record DecideResult<S, E>(S state, List<E> events) {
    @SafeVarargs
    public static <S, E> DecideResult<S, E> of(S state, E... events) {
        return new DecideResult<>(state, List.of(events));
    }
}
