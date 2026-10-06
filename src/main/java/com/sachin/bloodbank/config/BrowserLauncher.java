package com.sachin.bloodbank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

/**
 * Opens the default browser to the app's home page automatically once
 * Spring Boot finishes starting — so running `mvn spring-boot:run` (or the
 * packaged jar) from a terminal needs no extra manual step.
 *
 * Safe on servers/deployment (Railway, Docker, CI, etc.): Desktop.browse()
 * is only supported on a machine with an actual desktop/GUI session, so
 * this silently no-ops (just logs the URL) anywhere headless.
 */
@Component
public class BrowserLauncher {
    private static final Logger log = LoggerFactory.getLogger(BrowserLauncher.class);
    private final Environment env;

    public BrowserLauncher(Environment env) {
        this.env = env;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openBrowser() {
        if (!env.getProperty("app.auto-open-browser", Boolean.class, true)) {
            return;
        }

        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        String url = "http://localhost:" + port + "/";

        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
                log.info("Opened {} in your default browser.", url);
                return;
            }
        } catch (Exception e) {
            log.warn("Could not auto-open browser ({}). Visit {} manually.", e.getMessage(), url);
            return;
        }
        log.info("No desktop session detected (normal on a server) — visit {} manually.", url);
    }
}
