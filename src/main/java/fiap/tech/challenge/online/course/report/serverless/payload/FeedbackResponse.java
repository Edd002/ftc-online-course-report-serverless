package fiap.tech.challenge.online.course.report.serverless.payload;

public record FeedbackResponse(Boolean urgent, String description, String comment, String studentName, String studentEmail) {
}