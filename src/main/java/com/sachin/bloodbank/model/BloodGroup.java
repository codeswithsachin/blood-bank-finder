package com.sachin.bloodbank.model;

import java.util.EnumSet;
import java.util.Set;

public enum BloodGroup {
    A_POSITIVE("A+"), A_NEGATIVE("A-"),
    B_POSITIVE("B+"), B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"), AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"), O_NEGATIVE("O-");

    private final String symbol;

    BloodGroup(String symbol) {
        this.symbol = symbol;
    }

    /** Clean display form for the UI — e.g. "O+" instead of the raw enum name "O_POSITIVE". */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Real donor-compatibility matrix (standard transfusion medicine rules),
     * used by the matching engine to suggest compatible donors, not just
     * exact blood-group matches. O- is the universal donor; AB+ is the
     * universal recipient.
     */
    public boolean canDonateTo(BloodGroup recipient) {
        return compatibleRecipients().contains(recipient);
    }

    public Set<BloodGroup> compatibleRecipients() {
        return switch (this) {
            case O_NEGATIVE -> EnumSet.allOf(BloodGroup.class);
            case O_POSITIVE -> EnumSet.of(O_POSITIVE, A_POSITIVE, B_POSITIVE, AB_POSITIVE);
            case A_NEGATIVE -> EnumSet.of(A_NEGATIVE, A_POSITIVE, AB_NEGATIVE, AB_POSITIVE);
            case A_POSITIVE -> EnumSet.of(A_POSITIVE, AB_POSITIVE);
            case B_NEGATIVE -> EnumSet.of(B_NEGATIVE, B_POSITIVE, AB_NEGATIVE, AB_POSITIVE);
            case B_POSITIVE -> EnumSet.of(B_POSITIVE, AB_POSITIVE);
            case AB_NEGATIVE -> EnumSet.of(AB_NEGATIVE, AB_POSITIVE);
            case AB_POSITIVE -> EnumSet.of(AB_POSITIVE);
        };
    }
}
