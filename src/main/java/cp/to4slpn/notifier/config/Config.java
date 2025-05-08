package cp.to4slpn.notifier.config;

import java.time.Duration;
import java.time.LocalDate;

public record Config(UserCredentials credentials, MonitoringConfig monitoring, ExamConfig exam) {
    public record UserCredentials(String username, String password) {
    }

    public record MonitoringConfig(Duration timeout) {
    }

    public record ExamConfig(String category, String wordId, Duration checkInterval, LocalDate startDate,
                             LocalDate endDate, String examType) {
    }
}