package org.sopt.makers.crew.main.global.config.lambda;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.sopt.makers.crew.main.MainApplication;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.LambdaContainerHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.HttpApiV2ProxyRequest;
import com.amazonaws.serverless.proxy.spring.SpringBootLambdaContainerHandler;
import com.amazonaws.serverless.proxy.spring.SpringBootProxyHandlerBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LambdaHandler implements RequestStreamHandler {

	// HTTP API (v2) 형식 사용 - template의 Type: HttpApi와 일치
	private static final SpringBootLambdaContainerHandler<HttpApiV2ProxyRequest, AwsProxyResponse> handler;
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		long startTime = System.currentTimeMillis();

		try {
			System.out.println("lambda handler init started");
			log.info("Lambda Handler 초기화 시작...");

			handler = new SpringBootProxyHandlerBuilder<HttpApiV2ProxyRequest>()
				.defaultHttpApiV2Proxy()
				.servletApplication()
				.springBootApplication(MainApplication.class)
				.profiles("lambda-dev")
				.buildAndInitialize();

			LambdaContainerHandler.getContainerConfig().addBinaryContentTypes(
				"image/png",
				"image/jpeg",
				"image/gif",
				"application/octet-stream");

			long duration = System.currentTimeMillis() - startTime;

			System.out.println("lambda handler initialized durationMs=" + duration);
			log.info("Lambda Handler 초기화 완료 (소요 시간: {}ms)", duration);
		} catch (ContainerInitializationException e) {
			log.error("Spring Boot 애플리케이션 초기화 실패", e);
			throw new RuntimeException("Lambda Handler 초기화 실패 - CloudWatch Logs를 확인하세요", e);
		}
	}

	@Override
	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
		throws IOException {
		byte[] inputBytes = inputStream.readAllBytes();

		ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

		try {
			handler.proxyStream(
				new ByteArrayInputStream(inputBytes),
				capturedOutput,
				context
			);

			// outputStream.write(capturedOutput.toByteArray());
			// outputStream.flush();

			context.getLogger().log("lambda handler completed\n");
		} catch (Exception e) {
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			context.getLogger().log("lambda handler failed:\n" + stackTrace + "\n");
			throw e;
		}

	}
}
