package fiap.tech.challenge.online.course.report.serverless.payload;

public record FeedbackRequest(UserTypeRequest userType, String email, String accessKey, String studentEmail, String assessmentName, AssessmentType assessmentType, Double assessmentScore, Boolean feedbackUrgent, String feedbackDescription, String feedbackComment) {
}