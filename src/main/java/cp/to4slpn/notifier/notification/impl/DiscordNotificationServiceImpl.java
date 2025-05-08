package cp.to4slpn.notifier.notification.impl;

import cp.to4slpn.notifier.notification.NotificationService;
import okhttp3.*;

import java.io.IOException;

public final class DiscordNotificationServiceImpl implements NotificationService {
    private static final MediaType JSON = MediaType.get("application/json");
    private final OkHttpClient httpClient;
    private final String webhookUrl;

    public DiscordNotificationServiceImpl(OkHttpClient httpClient, String webhookUrl) {
        this.httpClient = httpClient;
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void sendNotification(String message) {
        String payload = String.format("{\"content\":\"%s\"}", "@everyone " + message);

        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(RequestBody.create(payload, JSON))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Failed to send notification: " + response.code());
            } else {
                System.out.println("Notification sent successfully!");
            }
        } catch (IOException e) {
            System.err.println("Notification sending failed: " + e.getMessage());
        }
    }
}
