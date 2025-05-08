package cp.to4slpn.notifier.api.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cp.to4slpn.notifier.api.exception.ApiException;
import cp.to4slpn.notifier.api.exception.AuthException;
import cp.to4slpn.notifier.api.model.ExamScheduleResponse;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class InfoCarClient {
    private static final String EXAM_SCHEDULE_URL = "https://info-car.pl/api/word/word-centers/exam-schedule";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final OkHttpClient client;
    private final InfoCarAuthClient authClient;
    private final ObjectMapper objectMapper;

    public InfoCarClient(OkHttpClient client, InfoCarAuthClient authClient) {
        this.client = client;
        this.authClient = authClient;
        this.objectMapper = new ObjectMapper();
    }

    public ExamScheduleResponse fetchExamSchedule(
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String wordId
    ) throws ApiException {
        try {
            String token = authClient.getAccessToken();
            Request request = buildRequest(token, startDate, endDate, category, wordId);

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new ApiException("API request failed with code: " + response.code());
                }

                ResponseBody body = response.body();
                if (body == null) {
                    throw new ApiException("Empty response body");
                }

                return objectMapper.readValue(body.string(), ExamScheduleResponse.class);
            }
        } catch (IOException e) {
            throw new ApiException("Failed to fetch exam schedule", e);
        } catch (AuthException e) {
            throw new RuntimeException(e);
        }
    }

    private Request buildRequest(
            String token,
            LocalDate startDate,
            LocalDate endDate,
            String category,
            String wordId
    ) {
        String requestBody = String.format(
                "{\"category\":\"%s\",\"startDate\":\"%s\",\"endDate\":\"%s\",\"wordId\":\"%s\"}",
                category,
                startDate.format(DATE_FORMATTER),
                endDate.format(DATE_FORMATTER),
                wordId
        );

        return new Request.Builder()
                .url(EXAM_SCHEDULE_URL)
                .header("Authorization", "Bearer " + token)
                .put(RequestBody.create(requestBody, JSON))
                .build();
    }
}