package fiap.tech.challenge.online.course.report.serverless.config;

import java.util.Properties;

public class EmailConfig {

    private final String url;
    private final String host;
    private final int port;
    private final String sender;
    private final String username;
    private final String password;
    private final boolean smtpAuth;
    private final boolean starttlsEnable;
    private final String sslProtocol;

    public EmailConfig(Properties applicationProperties) {
        this.url = applicationProperties.getProperty("application.mail.url");
        this.host = applicationProperties.getProperty("application.mail.host");
        this.port = Integer.parseInt(applicationProperties.getProperty("application.mail.port"));
        this.sender = applicationProperties.getProperty("application.mail.sender");
        this.username = applicationProperties.getProperty("application.mail.username");
        this.password = applicationProperties.getProperty("application.mail.password");
        this.smtpAuth = Boolean.parseBoolean(applicationProperties.getProperty("application.mail.smtp.auth"));
        this.starttlsEnable = Boolean.parseBoolean(applicationProperties.getProperty("application.mail.smtp.starttls.enable"));
        this.sslProtocol = applicationProperties.getProperty("application.mail.ssl.protocol");
    }

    public String getUrl() {
        return url;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return String.valueOf(port);
    }

    public String getSender() {
        return sender;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String isSmtpAuth() {
        return String.valueOf(smtpAuth);
    }

    public String isStarttlsEnable() {
        return String.valueOf(starttlsEnable);
    }

    public String getSslProtocol() {
        return sslProtocol;
    }
}