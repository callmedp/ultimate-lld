package com.ultimatelld.problems.moviebooking.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;

import java.util.Set;

/** Test gateway: approves everyone except a configurable set of "declined" users. */
public final class AlwaysApprovePaymentGateway implements PaymentGateway {

    private final Set<String> declinedUsers;

    public AlwaysApprovePaymentGateway(Set<String> declinedUsers) {
        this.declinedUsers = Set.copyOf(declinedUsers);
    }

    @Override
    public boolean charge(UserId userId, Money amount) {
        return !declinedUsers.contains(userId.value());
    }
}
