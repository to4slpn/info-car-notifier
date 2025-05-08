package cp.to4slpn.notifier.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExamSlot(String id, LocalDateTime dateTime) {
}