package fiap.tech.challenge.online.course.report.serverless.email;

import fiap.tech.challenge.online.course.report.serverless.config.EmailConfig;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportResponse;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class FTCOnlineCourseReportEmailDeliverService {

    public void sendEmailUrgentFeedback(FeedbackReportRequest feedbackReportRequest, FeedbackReportResponse feedbackReportResponse) {
        EmailConfig emailConfig = new EmailConfig();
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
            Address[] toUser = InternetAddress.parse("emailadministrador@emai.com");
            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject("E-mail de notificação de feedback de aluno");
            message.setText("Segue o relatório de feedback urgente do aluno de nome Nome Aluno e e-mail emailaluno@email.com:");

            Transport.send(message);
            System.out.println("E-mail enviado com sucesso.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}