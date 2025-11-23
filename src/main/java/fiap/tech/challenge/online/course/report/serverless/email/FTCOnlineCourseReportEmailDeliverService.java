package fiap.tech.challenge.online.course.report.serverless.email;

import fiap.tech.challenge.online.course.report.serverless.config.EmailConfig;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportResponse;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class FTCOnlineCourseReportEmailDeliverService {

    private final EmailConfig emailConfig;

    public FTCOnlineCourseReportEmailDeliverService(Properties applicationProperties) {
        emailConfig = new EmailConfig(applicationProperties);
    }

    public void sendEmailUrgentFeedback(FeedbackReportResponse feedbackReportResponse) {
        try {
            Properties props = new Properties();
            props.setProperty("mail.smtp.host", emailConfig.getHost());
            props.setProperty("mail.smtp.port", emailConfig.getPort());
            props.setProperty("mail.smtp.auth", emailConfig.isSmtpAuth());
            props.setProperty("mail.smtp.starttls.enable", emailConfig.isStarttlsEnable());
            props.setProperty("mail.smtp.ssl.protocols", emailConfig.getSslProtocol());

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailConfig.getUsername(), emailConfig.getPassword());
                        }
                    });
            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getSender()));
            Address[] toUser = InternetAddress.parse(feedbackReportResponse.administratorEmail());
            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject("E-mail de notificação de feedback urgente do aluno");
            message.setContent(buildEmailHtmlMessageBody(feedbackReportResponse), "text/html");

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