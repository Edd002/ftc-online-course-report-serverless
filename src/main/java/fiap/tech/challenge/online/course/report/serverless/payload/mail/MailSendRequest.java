package fiap.tech.challenge.online.course.report.serverless.payload.mail;

import java.util.List;

public record MailSendRequest(MailFromSendRequest from, List<MailToSendRequest> to, String subject, String html, String category) {
}