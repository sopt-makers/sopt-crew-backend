package org.sopt.makers.crew.main.global.config.lambda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sopt.makers.crew.main.MainApplication;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LambdaHandler implements RequestStreamHandler {

	private static final SpringBootLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse> handler;

	static {
		try {
			log.info("Lambda Handler 초기화 시작...");

			handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(MainApplication.class);

			LambdaContainerHandler.getContainerConfig().addBinaryContentTypes(
				"image/png",
				"image/jpeg",
				"image/gif",
				"application/octet-stream");

		} catch (ContainerInitializationException e) {
			throw new RuntimeException("Could not initialize Spring Boot application", e);
		} catch (Exception e) {
			log.error("Spring Boot 애플리케이션 초기화 실패", e);
			throw new RuntimeException("Could not initialize Lambda handler", e);
		}
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
		throws IOException {
		handler.proxyStream(inputStream, outputStream, context);
	}
}
