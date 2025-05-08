package cp.to4slpn.notifier.monitor;

import cp.to4slpn.notifier.api.client.InfoCarClient;
import cp.to4slpn.notifier.api.model.ExamScheduleResponse;
import cp.to4slpn.notifier.notification.NotificationService;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ExamMonitor {
    private final InfoCarClient infoCarClient;
    private final NotificationService notificationService;
    private final String examType;
    private final String category;
    private final String wordId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Duration checkInterval;

    private final Set<String> notifiedSlots = new HashSet<>();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    public ExamMonitor(
            InfoCarClient infoCarClient,
            NotificationService notificationService,
            String examType,
            String category,
            String wordId,
            LocalDate startDate,
            LocalDate endDate,
            Duration checkInterval
    ) {
        this.infoCarClient = infoCarClient;
        this.notificationService = notificationService;
        this.examType = examType;
        this.category = category;
        this.wordId = wordId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checkInterval = checkInterval;
    }

    public void start() {
        while (true) {
            try {
                checkForSlots();
                Thread.sleep(checkInterval.toMillis());
            } catch (Exception e) {
                System.err.println("Error during monitoring: " + e.getMessage());
                try {
                    Thread.sleep(checkInterval.toMillis());
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    private void checkForSlots() throws Exception {
        ExamScheduleResponse schedule = infoCarClient.fetchExamSchedule(startDate, endDate, category, wordId);

        List<ExamScheduleResponse.ScheduledDay> validDays = schedule.schedule().scheduledDays().stream()
                .filter(day -> day.hours().stream().anyMatch(this::shouldNotify))
                .toList();

        if (!validDays.isEmpty()) {
            String firstDate = validDays.getFirst().date();
            int validDayCount = validDays.size();
            String message = String.format("Found %d valid day(s). Soonest available: %s", validDayCount, firstDate);

            if (notifiedSlots.add(firstDate)) {
                notificationService.sendNotification(message);
            }
        }
    }

    private boolean shouldNotify(ExamScheduleResponse.ScheduledHour hour) {
        boolean notifyPractice = "practice".equalsIgnoreCase(examType) || "both".equalsIgnoreCase(examType);
        boolean notifyTheory = "theory".equalsIgnoreCase(examType) || "both".equalsIgnoreCase(examType);

        return (notifyPractice && !hour.practiceExams().isEmpty()) ||
                (notifyTheory && !hour.theoryExams().isEmpty());
    }
}