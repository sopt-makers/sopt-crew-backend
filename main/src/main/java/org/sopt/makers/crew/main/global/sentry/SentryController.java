package org.sopt.makers.crew.main.global.sentry;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sentry")
public class SentryController {

	@GetMapping
	public ResponseEntity<String> testSentry() { // prod에서 테스트 후 삭제
		try {
			throw new Exception("This is a test exception for Sentry.");
		} catch (Exception e) {
			Sentry.captureException(e);
			log.error("Exception captured in Sentry", e);
			return ResponseEntity.status(500).body("Exception captured in Sentry");
		}
	}
}
