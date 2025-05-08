package cp.to4slpn.notifier;

import cp.to4slpn.notifier.api.client.InfoCarAuthClient;
import cp.to4slpn.notifier.api.exception.AuthException;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;

import java.net.CookieManager;

public final class Main {
    public static void main(String[] args) {
        try {
            // setup http client
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(new CookieManager()))
                    .build();

            // initialize services
            InfoCarAuthClient authClient = new InfoCarAuthClient(
                    httpClient,
                    "example@example.com",
                    "ExamplePassword123"
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
