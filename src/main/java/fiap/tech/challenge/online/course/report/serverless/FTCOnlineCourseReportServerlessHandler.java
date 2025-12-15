package fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.report.serverless.config.KMSConfig;
import fiap.tech.challenge.online.course.report.serverless.dao.FTCOnlineCourseReportServerlessDAO;
import fiap.tech.challenge.online.course.report.serverless.email.FTCOnlineCourseReportEmailDeliverService;
import fiap.tech.challenge.online.course.report.serverless.loader.ApplicationPropertiesLoader;
import fiap.tech.challenge.online.course.report.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.payload.record.FeedbackReportRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.record.FeedbackReportResponse;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Properties;

public class FTCOnlineCourseReportServerlessHandler implements RequestHandler<SQSEvent, Void> {

    private static final KMSConfig kmsConfig;
    private static final Properties applicationProperties;
    private static final FTCOnlineCourseReportServerlessDAO ftcOnlineCourseReportServerlessDAO;
    private static final FTCOnlineCourseReportEmailDeliverService ftcOnlineCourseReportEmailDeliverService;

    static {
        try {
            kmsConfig = new KMSConfig();
            applicationProperties = ApplicationPropertiesLoader.loadProperties(kmsConfig);
            ftcOnlineCourseReportServerlessDAO = new FTCOnlineCourseReportServerlessDAO(applicationProperties);
            ftcOnlineCourseReportEmailDeliverService = new FTCOnlineCourseReportEmailDeliverService(applicationProperties);
        } catch (Exception ex) {
            System.err.println("Message: " + ex.getMessage() + " - Cause: " + ex.getCause() + " - Stacktrace: " + Arrays.toString(ex.getStackTrace()));
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        try {
            FeedbackReportRequest feedbackReportRequest = retrieveSQSMessageBody(event);
            if (feedbackReportRequest == null) {
                context.getLogger().log("Erro de conversão de payload de requisição.", LogLevel.ERROR);
                throw new InvalidParameterException("O payload para envio de e-mail de feedback urgente não foi informado corretamente.");
            }
            context.getLogger().log("Requisição recebida em FTC Online Course Report - hashIdFeedback: " + feedbackReportRequest.hashIdFeedback(), LogLevel.INFO);
            validateAPIGatewayProxyRequestEvent(feedbackReportRequest);
            FeedbackReportResponse feedbackReportResponse = ftcOnlineCourseReportServerlessDAO.getFeedbackReportByHashId(feedbackReportRequest);
            ftcOnlineCourseReportEmailDeliverService.sendEmailUrgentFeedbackByGmailSMTP(feedbackReportResponse);
            ftcOnlineCourseReportServerlessDAO.registerFeedbackReport(feedbackReportRequest, feedbackReportResponse);
            return null;
        } catch (Exception e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            throw new RuntimeException(e);
        }
    }

    private FeedbackReportRequest retrieveSQSMessageBody(SQSEvent event) {
        return event.getRecords().stream().findFirst().map(message -> HttpObjectMapper.readValue(message.getBody(), FeedbackReportRequest.class)).orElse(null);
    }

    private void validateAPIGatewayProxyRequestEvent(FeedbackReportRequest feedbackReportRequest) {
        try {
            if (feedbackReportRequest == null || feedbackReportRequest.hashIdFeedback() == null) {
                throw new InvalidParameterException("O hash id do feedback deve ser informado para que o e-mail de notificação de urgência seja enviado para o administrador.");
            }
        } catch (Exception e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }
}