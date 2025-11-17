package fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import fiap.tech.challenge.online.course.report.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.payload.FeedbackReportRequest;
import mock.TestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FTCOnlineCourseReportServerlessHandlerTests {

    @Test
    void handleRequest_SendAndRegisterReportSuccess() {
        FTCOnlineCourseReportServerlessHandler handler = new FTCOnlineCourseReportServerlessHandler();
        TestContext context = new TestContext();
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setHttpMethod("POST");
        request.setPath("/");
        request.setBody(HttpObjectMapper.writeValueAsString(new FeedbackReportRequest("ae440805-973b-4a1e-ad21-c4727193eb2a")));
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, context);
        assertEquals(201, response.getStatusCode().intValue());
    }
}