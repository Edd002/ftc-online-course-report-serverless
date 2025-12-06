package fiap.tech.challenge.online.course.report.serverless.email;

import fiap.tech.challenge.online.course.report.serverless.config.EmailConfig;
import fiap.tech.challenge.online.course.report.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.payload.record.FeedbackReportResponse;
import fiap.tech.challenge.online.course.report.serverless.payload.record.mail.MailFromSendRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.record.mail.MailSendRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.record.mail.MailToSendRequest;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;

public class FTCOnlineCourseReportEmailDeliverService {

    private final EmailConfig emailConfig;

    public FTCOnlineCourseReportEmailDeliverService(Properties applicationProperties) {
        emailConfig = new EmailConfig(applicationProperties);
    }

    public void sendEmailUrgentFeedbackByMailtrapAPI(FeedbackReportResponse feedbackReportResponse) {
        try (HttpClient client = HttpClient.newHttpClient()) {
            final String EMAIL_API_URL = emailConfig.getMailtrapUrl();
            final String EMAIL_API_TOKEN_KEY = emailConfig.getMailtrapPassword();

            String requestBody = HttpObjectMapper.writeValueAsString(
                    new MailSendRequest(
                            new MailFromSendRequest(emailConfig.getMailtrapSenderEmail(), "FTC Online Course Report"),
                            Collections.singletonList(new MailToSendRequest(feedbackReportResponse.administratorEmail())),
                            "E-mail de notificação de feedback urgente do aluno",
                            buildEmailHtmlMessageBody(feedbackReportResponse),
                            "Notification"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(EMAIL_API_URL))
                    .header("Authorization", "Bearer " + EMAIL_API_TOKEN_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(Objects.requireNonNull(requestBody)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Send e-mail request status code: " + response.statusCode());
            System.out.println("Send e-mail request response body: " + response.body());
        } catch (Exception ex) {
            System.err.println("Message: " + ex.getMessage() + " - Cause: " + ex.getCause() + " - Stacktrace: " + Arrays.toString(ex.getStackTrace()));
            throw new RuntimeException(ex);
        }
    }

    public void sendEmailUrgentFeedbackByGmailSMTP(FeedbackReportResponse feedbackReportResponse) {
        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", emailConfig.getGmailHost());
            props.setProperty("mail.smtp.port", emailConfig.getGmailPort());
            props.setProperty("mail.smtp.auth", emailConfig.isGmailSmtpAuth());
            props.setProperty("mail.smtp.starttls.enable", emailConfig.isGmailStarttlsEnable());
            props.setProperty("mail.smtp.ssl.protocols", emailConfig.getGmailSslProtocol());

            Session session = Session.getDefaultInstance(props,
                    new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailConfig.getGmailUsername(), emailConfig.getGmailPassword());
                        }
                    });
            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getGmailUsername(), "FTC Online Course Report"));
            Address[] toUser = InternetAddress.parse(feedbackReportResponse.administratorEmail());
            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject("E-mail de notificação de feedback urgente do aluno");
            message.setContent(buildEmailHtmlMessageBody(feedbackReportResponse), "text/html");

            MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
            mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
            CommandMap.setDefaultCommandMap(mc);

            Transport.send(message);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String buildEmailHtmlMessageBody(FeedbackReportResponse feedbackReportResponse) {
        return "Segue o relatório de feedback urgente do aluno: " +
                "<br><b>Data de registro do feedback:</b> " + feedbackReportResponse.createdIn() +
                "<br><b>Nome do administrador:</b> " + feedbackReportResponse.administradorName() +
                "<br><b>E-mail do administrador:</b> " + feedbackReportResponse.administratorEmail() +
                "<br><b>Nome do professor:</b> " + feedbackReportResponse.teacherName() +
                "<br><b>E-mail do professor:</b> " + feedbackReportResponse.teacherEmail() +
                "<br><b>Nome do estudante:</b> " + feedbackReportResponse.studentName() +
                "<br><b>E-mail do estudante:</b> " + feedbackReportResponse.studentEmail() +
                "<br><b>Tipo da avaliação:</b> " + feedbackReportResponse.assessmentType() +
                "<br><b>Nome da avaliação:</b> " + feedbackReportResponse.assessmentName() +
                "<br><b>Nota da avaliação:</b> " + feedbackReportResponse.assessmentScore() +
                "<br><b>Descrição do feedback:</b> " + feedbackReportResponse.description() +
                "<br><b>Comentário do feedback:</b> " + feedbackReportResponse.comment();
    }
}