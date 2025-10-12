package com.fiap.tech.challenge.online.course.report.serverless;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

public class FTCOnlineCourseReportServerlessHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        var logger = context.getLogger();
        logger.log("Request received on - FTC Online Course Report - Payload: " + request.getBody());
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody("""
                        {
                            "sucesso": true
                        }
                        """)
                .withIsBase64Encoded(false);
    }
}