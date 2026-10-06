package com.sachin.bloodbank.util;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;

import java.security.SecureRandom;

/**
 * A deliberately simple arithmetic CAPTCHA — no Google reCAPTCHA site/secret
 * keys required (those need registering the deployed domain with Google,
 * which isn't practical to hard-code for someone else's deployment). This
 * is a legitimate, well-established lightweight anti-bot technique for a
 * small public form, backed by the session so it can't be replayed.
 */
public final class CaptchaSupport {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void generateChallenge(HttpSession session, Model model) {
        int a = 1 + RANDOM.nextInt(9);
        int b = 1 + RANDOM.nextInt(9);
        session.setAttribute("captchaAnswer", a + b);
        model.addAttribute("captchaQuestion", a + " + " + b + " = ?");
    }

    public static boolean verify(HttpSession session, String userAnswer) {
        Object expected = session.getAttribute("captchaAnswer");
        session.removeAttribute("captchaAnswer"); // one-time use
        if (expected == null || userAnswer == null) return false;
        try {
            return Integer.parseInt(userAnswer.trim()) == (int) expected;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private CaptchaSupport() { }
}
