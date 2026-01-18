package org.sopt.makers.crew.main.slack;

import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventRequestDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventRequestDto.SlackEmojiEventDeleteRequestDto;
import org.sopt.makers.crew.main.slack.dto.SlackEmojiEventRequestDto.SlackUpdateEmojiEventRequestDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Profile({"dev", "lambda-dev"})
@RestController
@RequestMapping("/slack")
@RequiredArgsConstructor
@Slf4j
public class SlackController {

	private final SlackMessageService slackMessageService;
	@Value("${slack.api-auth-token}")
	private String slackAuthToken;

	@Operation(summary = "이모지 이벤트 생성")
	@PostMapping("/emoji")
	public ResponseEntity<String> addEmoji(@RequestBody SlackEmojiEventRequestDto requestDto) {
		try {
			validateToken(requestDto.getIdentifiedPwd());
			slackMessageService.insertEvent(requestDto.toDto());
			return ResponseEntity.ok("Successfully added emoji");
		} catch (InvalidSlackTokenException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return handleError(e);
		}
	}

	@Operation(summary = "이모지 이벤트 업데이트")
	@PatchMapping("/emoji")
	public ResponseEntity<String> updateEmoji(@RequestBody SlackUpdateEmojiEventRequestDto requestDto) {
		try {
			validateToken(requestDto.getIdentifiedPwd());
			slackMessageService.updateEvent(requestDto.toDto());
			return ResponseEntity.ok("Successfully update emoji");
		} catch (InvalidSlackTokenException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return handleError(e);
		}
	}

	@Operation(summary = "이모지 이벤트 삭제")
	@DeleteMapping("/emoji")
	public ResponseEntity<String> deleteEmoji(
		@RequestBody SlackEmojiEventDeleteRequestDto requestDto) {
		try {
			validateToken(requestDto.getIdentifiedPwd());
			slackMessageService.deleteEvent(requestDto.toDto());
			return ResponseEntity.ok("Successfully deleted emoji");
		} catch (InvalidSlackTokenException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			return handleError(e);
		}
	}

	private void validateToken(String token) {
		if (!isValidToken(token)) {
			throw new InvalidSlackTokenException("Slack API Token is invalid");
		}
	}

	private boolean isValidToken(String providedToken) {
		return providedToken != null && providedToken.equals(slackAuthToken);
	}

	private ResponseEntity<String> handleError(Exception e) {
		log.error("Failed to emoji event", e);
		return ResponseEntity.internalServerError()
			.body(e.getMessage());
	}

	private static class InvalidSlackTokenException extends RuntimeException {
		public InvalidSlackTokenException(String message) {
			super(message);
		}
	}
}
