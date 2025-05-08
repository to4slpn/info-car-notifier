package cp.to4slpn.notifier.config;

import java.time.Duration;

public record Config(
        UserCredentials credentials,
        MonitoringConfig monitoring
) {
    public record UserCredentials(String username, String password) {
    }

    public record MonitoringConfig(Duration timeout) {
    }
}
