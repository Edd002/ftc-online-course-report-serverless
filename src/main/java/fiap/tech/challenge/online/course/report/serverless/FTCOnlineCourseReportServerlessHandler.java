package fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import fiap.tech.challenge.online.course.report.serverless.dao.FTCOnlineCourseFeedbackReportServerlessDAO;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackRequest;
import fiap.tech.challenge.online.course.report.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.payload.error.ErrorResponse;
import fiap.tech.challenge.online.course.report.serverless.payload.error.InvalidParameterErrorResponse;

import java.security.InvalidParameterException;

public class FTCOnlineCourseReportServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final FTCOnlineCourseFeedbackReportServerlessDAO ftcOnlineCourseFeedbackReportServerlessDAO;

    static {
        ftcOnlineCourseFeedbackReportServerlessDAO = new FTCOnlineCourseFeedbackReportServerlessDAO();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        FeedbackRequest feedbackRequest = HttpObjectMapper.readValue(request.getBody(), FeedbackRequest.class);
        if (feedbackRequest == null) {
            context.getLogger().log("Erro de conversão de payload de requisição.", LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(new RuntimeException("O payload para envio de e-mail de feedbac urgente não foi informado corretamente."));
        }
        try {
            context.getLogger().log("Requisição recebida em FTC Online Course Report - UserType: " + feedbackRequest.userType() + " - E-mail: " + feedbackRequest.email(), LogLevel.INFO);
            validateAPIGatewayProxyRequestEvent(feedbackRequest);
            Long teacherId = ftcOnlineCourseFeedbackReportServerlessDAO.getTeacherIdByEmailAndAccessKey(feedbackRequest);
            Long teacherStudentId = ftcOnlineCourseFeedbackReportServerlessDAO.getTeacherStudentIdByTeacherIdAndStudentEmail(teacherId, feedbackRequest);
            ftcOnlineCourseFeedbackReportServerlessDAO.registerFeedback(teacherStudentId, feedbackRequest);
            return new APIGatewayProxyResponseEvent().withStatusCode(201).withIsBase64Encoded(false);
        } catch (InvalidParameterException e) {
            context.getLogger().log(e.getMessage(), LogLevel.ERROR);
            return buildInvalidParameterErrorResponse(e);
        } catch (Exception e) {
            context.getLogger().log(e.getMessage(), LogLevel.ERROR);
            return buildErrorResponse(feedbackRequest, e);
        }
    }

    private void validateAPIGatewayProxyRequestEvent(FeedbackRequest feedbackRequest) {
        try {
            if (feedbackRequest == null || feedbackRequest.userType() == null || feedbackRequest.email() == null || feedbackRequest.accessKey() == null) {
                throw new InvalidParameterException("O tipo de usuário juntamente com seu o e-mail e chave de acesso são obrigatórios para realizar a cadastro de feedback.");
            }
        } catch (Exception e) {
            throw new InvalidParameterException(e.getMessage());
        }
    }

    private APIGatewayProxyResponseEvent buildInvalidParameterErrorResponse(Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody(HttpObjectMapper.writeValueAsString(new InvalidParameterErrorResponse(e.getMessage()))).withIsBase64Encoded(false);
    }

    private APIGatewayProxyResponseEvent buildErrorResponse(FeedbackRequest feedbackRequest, Exception e) {
        return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody(HttpObjectMapper.writeValueAsString(new ErrorResponse(feedbackRequest.userType(), feedbackRequest.email(), e.getMessage()))).withIsBase64Encoded(false);
    }
}