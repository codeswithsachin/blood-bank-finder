package com.sachin.bloodbank.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thymeleaf 3.1 (the version bundled with this Spring Boot release) removed
 * the '#request', '#session', '#servletContext' and '#response' expression
 * utility objects from templates entirely — using them now throws
 * IllegalArgumentException at render time, it's not a config mistake on the
 * running machine. The fix Thymeleaf's own docs recommend is exactly this:
 * expose whatever you need as a normal model attribute instead.
 *
 * currentUri / currentUrl are added to every page's model automatically, so
 * templates use ${currentUri} / ${currentUrl} instead of #request.*.
 * Used by the language-switcher links (nav) and the WhatsApp share link
 * (request-blood.html).
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("currentUrl")
    public String currentUrl(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }
}
