package fiap.tech.challenge.online.course.report.serverless.payload.record.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, String cause) {
}