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
		long startTime = System.currentTimeMillis();

		try {
			log.info("Lambda Handler 초기화 시작...");

			handler = SpringBootLambdaContainerHandler.getAwsProxyHandler(MainApplication.class);

			LambdaContainerHandler.getContainerConfig().addBinaryContentTypes(
				"image/png",
				"image/jpeg",
				"image/gif",
				"application/octet-stream");

			long duration = System.currentTimeMillis() - startTime;

			log.info("Lambda Handler 초기화 완료 (소요 시간: {}ms)", duration);
		} catch (ContainerInitializationException e) {
			log.error("Spring Boot 애플리케이션 초기화 실패", e);
			throw new RuntimeException("Lambda Handler 초기화 실패 - CloudWatch Logs를 확인하세요", e);
		}
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
		throws IOException {
		handler.proxyStream(inputStream, outputStream, context);
	}
}
