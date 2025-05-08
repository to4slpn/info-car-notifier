package cp.to4slpn.notifier;

import cp.to4slpn.notifier.api.client.InfoCarAuthClient;
import cp.to4slpn.notifier.api.exception.AuthException;
import cp.to4slpn.notifier.config.Config;
import cp.to4slpn.notifier.config.ConfigLoader;
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

            // setup http client
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(new CookieManager()))
                    .connectTimeout(config.monitoring().timeout())
                    .callTimeout(config.monitoring().timeout())
                    .build();

            // initialize services
            InfoCarAuthClient authClient = new InfoCarAuthClient(
                    httpClient,
                    config.credentials().username(),
                    config.credentials().password()
            );

            authClient.login();
        } catch (AuthException e) {
            System.err.println("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
