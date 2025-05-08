package cp.to4slpn.notifier.api.client;

import cp.to4slpn.notifier.api.exception.AuthException;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class InfoCarAuthClient {
    private static final String LOGIN_URL = "https://info-car.pl/oauth2/login";
    private static final String REFRESH_URL = "https://info-car.pl/oauth2/authorize";

    private final OkHttpClient client;
    private final String username;
    private final String password;

    private String accessToken;
    private Instant tokenExpiry;

    public InfoCarAuthClient(OkHttpClient client, String username, String password) {
        this.client = client;
        this.username = username;
        this.password = password;
    }

    public synchronized void login() throws AuthException {
        try {
            String csrfToken = fetchCsrfTokenFromLoginPage();

            submitLoginForm(csrfToken);

            refreshToken();
        } catch (IOException e) {
            throw new AuthException("Login process failed", e);
        }
    }

    private String fetchCsrfTokenFromLoginPage() throws IOException, AuthException {
        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new AuthException("Failed to fetch login page: " + response.code());
            }

            String html = response.body().string();
            Document doc = Jsoup.parse(html);
            Element csrfElement = doc.selectFirst("input[name=_csrf]");

            if (csrfElement == null || csrfElement.attr("value").isEmpty()) {
                throw new AuthException("CSRF token not found in login page HTML");
            }

            return csrfElement.attr("value");
        }
    }

    private void submitLoginForm(String csrfToken) throws IOException, AuthException {
        FormBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("_csrf", csrfToken)
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(formBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AuthException("Login failed: " + response.code());
            }
        }
    }

    private void refreshToken() throws IOException, AuthException {
        HttpUrl url = Objects.requireNonNull(HttpUrl.parse(REFRESH_URL)).newBuilder()
                .addQueryParameter("response_type", "id_token token")
                .addQueryParameter("client_id", "client")
                .addQueryParameter("redirect_uri", "https://info-car.pl/new/assets/refresh.html")
                .addQueryParameter("scope", "openid profile email resource.read")
                .addQueryParameter("prompt", "none")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String fragment = response.request().url().fragment();
            if (fragment == null) {
                throw new AuthException("No fragment in response");
            }

            Map<String, String> params = parseFragment(fragment);
            accessToken = params.get("access_token");
            if (accessToken == null) {
                throw new AuthException("No access token in response");
            }
            System.out.println("Logged in. Access token: " + accessToken);
            String expiresIn = params.get("expires_in");
            if (expiresIn != null) {
                tokenExpiry = Instant.now().plusSeconds(Long.parseLong(expiresIn));
            }
        }
    }

    private Map<String, String> parseFragment(String fragment) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = fragment.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            params.put(pair.substring(0, idx), pair.substring(idx + 1));
        }
        return params;
    }

    public String getAccessToken() throws AuthException {
        if (accessToken == null || (tokenExpiry != null && Instant.now().isAfter(tokenExpiry))) {
            try {
                refreshToken();
            } catch (IOException | AuthException e) {
                throw new AuthException("Token refresh failed", e);
            }
        }
        return accessToken;
    }
}