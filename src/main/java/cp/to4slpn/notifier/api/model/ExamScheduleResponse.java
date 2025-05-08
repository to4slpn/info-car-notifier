package cp.to4slpn.notifier.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true) //todo:: different way ts paki af?
public record ExamScheduleResponse(@JsonProperty("organizationId") String organizationId,
                                   @JsonProperty("schedule") Schedule schedule) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Schedule(@JsonProperty("scheduledDays") List<ScheduledDay> scheduledDays) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduledDay(@JsonProperty("day") String date,
                               @JsonProperty("scheduledHours") List<ScheduledHour> hours) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduledHour(@JsonProperty("time") String time,
                                @JsonProperty("theoryExams") List<ExamSlot> theoryExams,
                                @JsonProperty("practiceExams") List<ExamSlot> practiceExams) {
    }
}
