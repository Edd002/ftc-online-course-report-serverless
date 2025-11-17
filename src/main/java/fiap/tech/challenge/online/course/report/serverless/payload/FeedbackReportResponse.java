package fiap.tech.challenge.online.course.report.serverless.payload;

public record FeedbackReportResponse(Boolean urgent, String description, String comment, String administradorName, String administratorEmail, String teacherName, String teacherEmail, String studentName, String studentEmail, String assessmentName, AssessmentType assessmentType, Double assessmentScore, String createdIn) {
}