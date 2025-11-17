package fiap.tech.challenge.online.course.report.serverless.email;

import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportResponse;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class FTCOnlineCourseReportEmailDeliverService {

    public void sendEmailUrgentFeedback(FeedbackReportRequest feedbackReportRequest, FeedbackReportResponse feedbackReportResponse) {
        try {
            final String userName = "rm361276@fiap.com.br";
            final String password = "123";

            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");
            props.setProperty("mail.smtp.starttls.enable", "true");
            props.setProperty("mail.smtp.host", "outlook.office365.com");
            props.setProperty("mail.smtp.port", "587");
            props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(userName, password);
                        }
                    });
            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("rm361276@fiap.com.br"));
            Address[] toUser = InternetAddress.parse("edduarddollima@gmail.com");
            message.setRecipients(Message.RecipientType.TO, toUser);
            message.setSubject("Enviando email com JavaMail");
            message.setText("Enviei este email utilizando JavaMail com minha conta Microsoft.");

            Transport.send(message);
            System.out.println("E-mail enviado com sucesso.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}