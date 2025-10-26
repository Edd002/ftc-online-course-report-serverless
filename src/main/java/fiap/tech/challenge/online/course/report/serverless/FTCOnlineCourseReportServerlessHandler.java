package fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.tech.challenge.online.course.report.serverless.request.LoginRequest;
import fiap.tech.challenge.online.course.report.serverless.response.LoginResponse;

import java.io.IOException;
import java.io.UncheckedIOException;

public class FTCOnlineCourseReportServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final ObjectMapper objectMapper;

    static {
        // Dependence initialization, database connection, constant definition, etc.
        objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();
        try {
            logger.log("Request received on - FTC Online Course Report - Payload: " + request.getBody());

            var loginRequest = objectMapper.readValue(request.getBody(), LoginRequest.class);
            var isAuthorized = loginRequest.username().equalsIgnoreCase("admin") && loginRequest.password().equalsIgnoreCase("admin");
            var loginResponse = new LoginResponse(isAuthorized);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(objectMapper.writeValueAsString(loginResponse))
                    .withIsBase64Encoded(false);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}