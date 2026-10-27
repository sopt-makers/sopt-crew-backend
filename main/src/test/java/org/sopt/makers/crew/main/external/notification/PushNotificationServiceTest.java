package org.sopt.makers.crew.main.external.notification;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.external.notification.dto.request.PushNotificationRequestDto;
import org.sopt.makers.crew.main.external.notification.dto.response.PushNotificationResponseDto;

class PushNotificationServiceTest {

	private final PushNotificationServerClient client = mock(PushNotificationServerClient.class);
	private final PushNotificationService service = new PushNotificationService(client);
	private final PushNotificationRequestDto request = PushNotificationRequestDto.of(
		new String[] {"1"}, "제목", "본문", "NEWS", "https://crew.test");

	@Test
	@DisplayName("push 서버 호출이 성공하면 true를 반환한다.")
	void returnsTrueOnSuccess() {
		assertThat(service.sendPushNotification(request)).isTrue();
	}

	@Test
	@DisplayName("2xx 응답이어도 본문의 success가 false면 false를 반환한다.")
	void returnsFalseWhenResponseSuccessIsFalse() {
		when(client.sendPushNotification(any(), any(), any(), any(), any()))
			.thenReturn(new PushNotificationResponseDto(200, false, "invalid user"));

		assertThat(service.sendPushNotification(request)).isFalse();
	}

	@Test
	@DisplayName("success가 true이거나 본문·success 값이 없는 2xx 응답은 성공으로 본다.")
	void returnsTrueWhenResponseSuccessIsTrueOrMissing() {
		when(client.sendPushNotification(any(), any(), any(), any(), any()))
			.thenReturn(new PushNotificationResponseDto(200, true, null))
			.thenReturn(new PushNotificationResponseDto(200, null, null))
			.thenReturn(null);

		assertThat(service.sendPushNotification(request)).isTrue();
		assertThat(service.sendPushNotification(request)).isTrue();
		assertThat(service.sendPushNotification(request)).isTrue();
	}

	@Test
	@DisplayName("push 서버 호출이 실패하면 예외를 전파하지 않고 false를 반환한다.")
	void returnsFalseWithoutThrowingOnFailure() {
		when(client.sendPushNotification(any(), any(), any(), any(), any()))
			.thenThrow(new RuntimeException("push server down"));

		assertThat(service.sendPushNotification(request)).isFalse();
	}
}
