package com.sachin.bloodbank.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

/**
 * Enables the Hindi/English language toggle. The chosen language is stored
 * in a cookie (not the session) so it persists across visits without
 * requiring login. Switch language anywhere by adding ?lang=hi or ?lang=en
 * to any URL (see the nav bar links in fragments.html).
 *
 * Coverage note: translated strings live in messages.properties (English)
 * and messages_hi.properties (Hindi) at the classpath root. As of this
 * version, translation covers the public-facing pages (nav, landing,
 * login, register, contact, request-blood) — the donor/admin dashboards
 * are not yet translated. See PROJECT_CONTEXT.md for the full list.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("app-locale");
        resolver.setDefaultLocale(Locale.ENGLISH);
        resolver.setCookieMaxAge(Duration.ofDays(365));
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
