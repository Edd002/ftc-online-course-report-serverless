package fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.report.serverless.dao.FTCOnlineCourseReportServerlessDAO;
import fiap.tech.challenge.online.course.report.serverless.email.FTCOnlineCourseReportEmailDeliverService;
import fiap.tech.challenge.online.course.report.serverless.loader.ApplicationPropertiesLoader;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportResponse;
import fiap.tech.challenge.online.course.report.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.payload.error.ErrorResponse;
import fiap.tech.challenge.online.course.report.serverless.payload.error.InvalidParameterErrorResponse;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Properties;

public class FTCOnlineCourseReportServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final Properties applicationProperties;
    private static final FTCOnlineCourseReportServerlessDAO ftcOnlineCourseReportServerlessDAO;
    private static final FTCOnlineCourseReportEmailDeliverService ftcOnlineCourseReportEmailDeliverService;

    static {
        try {
            applicationProperties = ApplicationPropertiesLoader.loadProperties();
            ftcOnlineCourseReportServerlessDAO = new FTCOnlineCourseReportServerlessDAO(applicationProperties);
            ftcOnlineCourseReportEmailDeliverService = new FTCOnlineCourseReportEmailDeliverService(applicationProperties);
        } catch (Exception ex) {
            System.err.println("Message: " + ex.getMessage() + " - Cause: " + ex.getCause() + " - Stacktrace: " + Arrays.toString(ex.getStackTrace()));
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            FeedbackReportRequest feedbackReportRequest = HttpObjectMapper.readValue(request.getBody(), FeedbackReportRequest.class);
            if (feedbackReportRequest == null) {
                context.getLogger().log("Erro de conversão de payload de requisição.", LogLevel.ERROR);
                return buildInvalidParameterErrorResponse(new InvalidParameterException("O payload para envio de e-mail de feedback urgente não foi informado corretamente."));
            }
            context.getLogger().log("Requisição recebida em FTC Online Course Report - hashIdFeedback: " + feedbackReportRequest.hashIdFeedback(), LogLevel.INFO);
            validateAPIGatewayProxyRequestEvent(feedbackReportRequest);
            FeedbackReportResponse feedbackReportResponse = ftcOnlineCourseReportServerlessDAO.getFeedbackReportByHashId(feedbackReportRequest);
            ftcOnlineCourseReportEmailDeliverService.sendEmailUrgentFeedback(feedbackReportResponse);
            ftcOnlineCourseReportServerlessDAO.registerFeedbackReport(feedbackReportRequest, feedbackReportResponse);
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withIsBase64Encoded(false);
        } catch (InvalidParameterException e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(e);
        } catch (Exception e) {
            context.getLogger().log("Message: " + e.getMessage() + " - Cause: " + e.getCause() + " - Stacktrace: " + Arrays.toString(e.getStackTrace()), LogLevel.ERROR);
            return buildErrorResponse(e);
        }
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

    private APIGatewayProxyResponseEvent buildInvalidParameterErrorResponse(InvalidParameterException e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(HttpObjectMapper.writeValueAsString(new InvalidParameterErrorResponse(e.getMessage(), e.getCause().toString()))).withIsBase64Encoded(false);
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(HttpObjectMapper.writeValueAsString(new ErrorResponse(e.getMessage(), e.getCause().toString()))).withIsBase64Encoded(false);
    }
}