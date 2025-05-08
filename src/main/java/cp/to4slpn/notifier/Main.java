package cp.to4slpn.notifier;

import cp.to4slpn.notifier.api.client.InfoCarAuthClient;
import cp.to4slpn.notifier.api.client.InfoCarClient;
import cp.to4slpn.notifier.api.exception.AuthException;
import cp.to4slpn.notifier.config.Config;
import cp.to4slpn.notifier.config.ConfigLoader;
import cp.to4slpn.notifier.monitor.ExamMonitor;
import cp.to4slpn.notifier.notification.NotificationService;
import cp.to4slpn.notifier.notification.impl.DiscordNotificationService;
import cp.to4slpn.notifier.notification.impl.TextNotificationService;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;

import java.io.InputStream;
import java.net.CookieManager;

public final class Main {
    public static void main(String[] args) {
        try {
            // load configuration
            InputStream configStream = Main.class.getResourceAsStream("/config.yml");
            if (configStream == null) {
                System.err.println("Could not find the config file.");
                return;
            }

            Config config = ConfigLoader.loadConfig(configStream);

            // setup httpclient
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(new CookieManager()))
                    .connectTimeout(config.monitoring().timeout())
                    .callTimeout(config.monitoring().timeout())
                    .build();

            // authenticate with infocar
            InfoCarAuthClient authClient = new InfoCarAuthClient(
                    httpClient,
                    config.credentials().username(),
                    config.credentials().password()
            );

            // perform login and establish session
            authClient.login();

            // initialize infocar api client with the authenticated session
            InfoCarClient infoCarClient = new InfoCarClient(httpClient, authClient);

            // initialize notification service
            NotificationService notificationService = null;
            if (config.notification().discord()) {
                notificationService = new DiscordNotificationService(httpClient,
                        config.notification().webhookUrl());
            } else {
                notificationService = new TextNotificationService();
            }


            // setup the exam monitoring logic
            ExamMonitor monitor = new ExamMonitor(
                    infoCarClient,
                    notificationService,
                    config.exam().examType(),
                    config.exam().category(),
                    config.exam().wordId(),
                    config.exam().startDate(),
                    config.exam().endDate(),
                    config.exam().checkInterval()
            );

            monitor.start();
        } catch (AuthException e) {
            System.err.println("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
