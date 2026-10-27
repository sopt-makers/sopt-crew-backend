package org.sopt.makers.crew.main.dining.v2;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeesResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeesCsvResponseDto;
import org.sopt.makers.crew.main.global.advice.ControllerExceptionAdvice;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendanceResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetPayerRequestsResponseDto;
import org.sopt.makers.crew.main.entity.dining.enums.DiningPayerStatus;
import org.sopt.makers.crew.main.dining.v2.service.DiningV2Service;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class DiningV2ControllerTest {

	@Mock
	private DiningV2Service diningV2Service;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new DiningV2Controller(diningV2Service))
			.setControllerAdvice(new ControllerExceptionAdvice()).build();
	}

	@Test
	void csvRoute_returnsUrlWithoutPagination() throws Exception {
		given(diningV2Service.getAttendeesCsv(1, 2, 3))
			.willReturn(new DiningV2GetAttendeesCsvResponseDto("https://image.test/attendees.csv"));
		mockMvc.perform(get("/dining/v2/1/rounds/2/attendees/csv").principal(() -> "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.url").value("https://image.test/attendees.csv"));
		verify(diningV2Service).getAttendeesCsv(1, 2, 3);
		verify(diningV2Service, never()).getAttendees(anyInt(), anyInt(), any(), anyInt());
	}

	@Test
	void csvRoute_returnsForbiddenForUnauthorizedUser() throws Exception {
		given(diningV2Service.getAttendeesCsv(1, 2, 3)).willThrow(new ForbiddenException());
		mockMvc.perform(get("/dining/v2/1/rounds/2/attendees/csv").principal(() -> "3"))
			.andExpect(status().isForbidden());
	}

	@Test
	void csvRoute_returnsNotFoundForWrongRound() throws Exception {
		given(diningV2Service.getAttendeesCsv(1, 2, 3)).willThrow(new NotFoundException());
		mockMvc.perform(get("/dining/v2/1/rounds/2/attendees/csv").principal(() -> "3"))
			.andExpect(status().isNotFound());
	}

	@Test
	void modalRoute_returnsOnlyPublicProfile() throws Exception {
		given(diningV2Service.getAttendanceAttendees(1, 2, 3)).willReturn(new DiningV2GetAttendeesResponseDto(
			List.of(new DiningV2GetAttendeesResponseDto.Attendee(4, "참석자", "profile.jpg"))));

		mockMvc.perform(get("/dining/v2/1/rounds/2/attendance/attendees").principal(() -> "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.attendees[0].id").value(4))
			.andExpect(jsonPath("$.attendees[0].name").value("참석자"))
			.andExpect(jsonPath("$.attendees[0].phone").doesNotExist())
			.andExpect(jsonPath("$.attendees[0].recentGeneration").doesNotExist());
	}

	@Test
	void managementRoute_bindsDefaultAndExplicitPagination() throws Exception {
		mockMvc.perform(get("/dining/v2/1/rounds/2/attendees").principal(() -> "3"))
			.andExpect(status().isOk());
		mockMvc.perform(get("/dining/v2/1/rounds/2/attendees")
			.param("page", "2").param("take", "50").param("date", "asc").principal(() -> "3"))
			.andExpect(status().isOk());

		var captor = ArgumentCaptor.forClass(DiningV2GetAttendeesQueryDto.class);
		verify(diningV2Service, times(2)).getAttendees(eq(1), eq(2), captor.capture(), eq(3));
		var defaults = captor.getAllValues().get(0);
		assertThat(defaults.getPage()).isEqualTo(1);
		assertThat(defaults.getTake()).isEqualTo(10);
		assertThat(defaults.getDate()).isEqualTo("desc");
		var explicit = captor.getAllValues().get(1);
		assertThat(explicit.getPage()).isEqualTo(2);
		assertThat(explicit.getTake()).isEqualTo(50);
		assertThat(explicit.getDate()).isEqualTo("asc");
	}

	@Test
	void managementRoute_rejectsInvalidPagination() throws Exception {
		for (String query : List.of("take=20", "page=0", "date=invalid")) {
			mockMvc.perform(get("/dining/v2/1/rounds/2/attendees?" + query).principal(() -> "3"))
				.andExpect(status().isBadRequest());
		}
		verifyNoInteractions(diningV2Service);
	}

	@Test
	void payerRoutes_bindPrincipalAndPathWithoutBody() throws Exception {
		mockMvc.perform(post("/dining/v2/1/rounds/2/payer-requests").principal(() -> "3"))
			.andExpect(status().isCreated());
		verify(diningV2Service).requestPayer(1, 2, 3);
		for (String action : List.of("approve", "reject", "revoke")) {
			mockMvc.perform(post("/dining/v2/1/rounds/2/payer-requests/3/" + action).principal(() -> "4"))
				.andExpect(status().isOk());
		}
		verify(diningV2Service).approvePayer(1, 2, 3, 4);
		verify(diningV2Service).rejectPayer(1, 2, 3, 4);
		verify(diningV2Service).revokePayer(1, 2, 3, 4);
	}

	@Test
	void payerRequestsRoute_serializesProfileRoundAndEmptyList() throws Exception {
		var requestedAt = java.time.LocalDateTime.of(2024, 4, 24, 0, 0);
		given(diningV2Service.getPayerRequests(1, 4)).willReturn(new DiningV2GetPayerRequestsResponseDto(
			List.of(new DiningV2GetPayerRequestsResponseDto.PayerRequest(3, "참석자", "profile.jpg", 2, 1, requestedAt))))
			.willReturn(new DiningV2GetPayerRequestsResponseDto(List.of()));
		mockMvc.perform(get("/dining/v2/1/payer-requests").principal(() -> "4"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.requests[0].userId").value(3))
			.andExpect(jsonPath("$.requests[0].name").value("참석자"))
			.andExpect(jsonPath("$.requests[0].profileImage").value("profile.jpg"))
			.andExpect(jsonPath("$.requests[0].roundId").value(2))
			.andExpect(jsonPath("$.requests[0].roundNumber").value(1))
			.andExpect(jsonPath("$.requests[0].requestedAt").exists());
		mockMvc.perform(get("/dining/v2/1/payer-requests").principal(() -> "4"))
			.andExpect(status().isOk()).andExpect(jsonPath("$.requests").isEmpty());
	}

	@Test
	void attendanceRoute_includesPayerStatus() throws Exception {
		given(diningV2Service.getAttendance(1, 2, 3))
			.willReturn(new DiningV2GetAttendanceResponseDto(2, true, DiningPayerStatus.PENDING));
		mockMvc.perform(get("/dining/v2/1/rounds/2/attendance").principal(() -> "3"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.isAttending").value(true))
			.andExpect(jsonPath("$.payerStatus").value("PENDING"));
	}
}
