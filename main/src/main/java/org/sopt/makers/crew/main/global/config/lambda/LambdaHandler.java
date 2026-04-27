package org.sopt.makers.crew.main.global.config.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sopt.makers.crew.main.MainApplication;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class LambdaHandler implements RequestStreamHandler {

	private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

	static {
		try {
			System.setProperty("spring.main.web-application-type", "servlet");

			handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(MainApplication.class);
		} catch (ContainerInitializationException e) {
			throw new RuntimeException("Could not initialize Spring Boot application", e);
		} catch (Exception e) {
			throw new RuntimeException("Could not initialize Lambda handler", e);
		}
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
		throws IOException {
		handler.proxyStream(inputStream, outputStream, context);
	}
}
