package fiap.tech.challenge.online.course.report.serverless.payload.error;

import fiap.tech.challenge.online.course.report.serverless.payload.UserTypeRequest;

public record ErrorResponse(UserTypeRequest userType, String email, String error) {
}