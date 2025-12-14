package fiap.tech.challenge.online.course.report.serverless.payload.record;

import com.fasterxml.jackson.annotation.JsonFormat;
import fiap.tech.challenge.online.course.report.serverless.payload.enumeration.AssessmentType;

import java.util.Date;

public record FeedbackReportResponse(Boolean urgent, String description, String comment, String administradorName, String administratorEmail, String teacherName, String teacherEmail, String studentName, String studentEmail, String assessmentName, AssessmentType assessmentType, Double assessmentScore, @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy hh:mm", timezone = "America/Sao_Paulo") Date createdIn) {
}