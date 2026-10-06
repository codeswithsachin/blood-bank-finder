package com.sachin.bloodbank.util;

import java.util.regex.Pattern;

/**
 * Validates Indian mobile numbers server-side. HTML5 `required` only checks
 * a field is non-empty and is trivially bypassed by anyone posting directly
 * to the endpoint (curl, Postman) — this is the real check.
 * Accepts: 10 digits starting 6-9, optionally prefixed with +91 or 0.
 */
public final class PhoneValidator {
    private static final Pattern INDIAN_MOBILE = Pattern.compile("^(?:\\+91|0)?[6-9]\\d{9}$");

    public static boolean isValid(String phone) {
        if (phone == null) return false;
        String cleaned = phone.replaceAll("[\\s-]", "");
        return INDIAN_MOBILE.matcher(cleaned).matches();
    }

    private PhoneValidator() { }
}
