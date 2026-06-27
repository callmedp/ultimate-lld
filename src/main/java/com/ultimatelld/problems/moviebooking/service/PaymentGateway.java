package com.ultimatelld.problems.moviebooking.service;

import com.ultimatelld.common.Money;
import com.ultimatelld.problems.moviebooking.entity.Ids.UserId;

/** External payment abstraction (DIP) — swap a real PSP in without touching the booking logic. */
public interface PaymentGateway {
    /** @return true if the charge succeeded. */
    boolean charge(UserId userId, Money amount);
}
