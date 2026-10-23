package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.dining.DiningAttendance;
import org.sopt.makers.crew.main.entity.dining.DiningAttendanceRepository;
import org.sopt.makers.crew.main.entity.dining.DiningRound;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DiningAttendanceConstraintTest {

	@Mock
	private MeetingRepository meetingRepository;
	@Mock
	private DiningRoundRepository diningRoundRepository;
	@Mock
	private DiningAttendanceRepository diningAttendanceRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private DiningPermissionValidator diningPermissionValidator;
	@InjectMocks
	private DiningV2ServiceImpl service;

	@BeforeEach
	void setUp() {
		DiningRound round = DiningRound.builder().meetingId(1).build();
		ReflectionTestUtils.setField(round, "id", 2);
		given(diningRoundRepository.findByIdOrThrow(2)).willReturn(round);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void duplicateConstraint_isTranslatedWithoutQueryingAbortedTransaction(boolean management) {
		var violation = new ConstraintViolationException("중복 참석", new SQLException("unique", "23505"),
			"UQ_dining_attendance_round_user");
		given(diningAttendanceRepository.saveAndFlush(any(DiningAttendance.class)))
			.willThrow(new DataIntegrityViolationException("저장 실패", new RuntimeException(violation)));

		assertThatThrownBy(() -> addAttendance(management))
			.isInstanceOf(BadRequestException.class)
			.hasMessageContaining(ALREADY_ATTENDED_DINING_ROUND.getErrorCode());
		verify(diningAttendanceRepository, times(1)).existsByRoundIdAndUserId(2, 3);
		verify(diningAttendanceRepository).saveAndFlush(any(DiningAttendance.class));
		verifyNoMoreInteractions(diningAttendanceRepository);
	}

	@ParameterizedTest
	@ValueSource(strings = {"other_constraint", ""})
	void otherIntegrityViolations_arePropagated(String constraint) {
		var violation = new ConstraintViolationException("다른 무결성 오류", new SQLException(),
			constraint.isEmpty() ? null : constraint);
		var original = new DataIntegrityViolationException("저장 실패", violation);
		given(diningAttendanceRepository.saveAndFlush(any(DiningAttendance.class))).willThrow(original);

		assertThatThrownBy(() -> addAttendance(false)).isSameAs(original);
		verify(diningAttendanceRepository, times(1)).existsByRoundIdAndUserId(2, 3);
		verify(diningAttendanceRepository).saveAndFlush(any(DiningAttendance.class));
		verifyNoMoreInteractions(diningAttendanceRepository);
	}

	private void addAttendance(boolean management) {
		if (management) {
			service.addAttendee(1, 2, 3, 4);
		} else {
			service.applyAttendance(1, 2, 3);
		}
	}
}
