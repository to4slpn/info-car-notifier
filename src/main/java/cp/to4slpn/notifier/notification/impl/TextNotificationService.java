package cp.to4slpn.notifier.notification.impl;

import cp.to4slpn.notifier.notification.NotificationService;

public final class TextNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println(message);
    }
}

