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
		mockMvc = MockMvcBuilders.standaloneSetup(new DiningV2Controller(diningV2Service)).build();
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
}
