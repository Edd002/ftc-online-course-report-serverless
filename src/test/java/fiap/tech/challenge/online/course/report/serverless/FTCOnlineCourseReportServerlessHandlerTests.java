package fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import fiap.tech.challenge.online.course.report.serverless.mock.TestContext;
import fiap.tech.challenge.online.course.report.serverless.payload.HttpObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.payload.record.FeedbackReportRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;

class FTCOnlineCourseReportServerlessHandlerTests {

    @Test
    void handleRequest_SendAndRegisterReportSuccess() {
        FTCOnlineCourseReportServerlessHandler handler = new FTCOnlineCourseReportServerlessHandler();
        TestContext context = new TestContext();
        SQSEvent event = new SQSEvent();
        SQSEvent.SQSMessage sqsMessage = new SQSEvent.SQSMessage();
        sqsMessage.setBody(HttpObjectMapper.writeValueAsString(new FeedbackReportRequest("29ec6f1e-572a-4aad-b096-53225b77aaa8")));
        event.setRecords(Collections.singletonList(sqsMessage));
        Void voidResponse = handler.handleRequest(event, context);
        assertNull(voidResponse);
    }
}