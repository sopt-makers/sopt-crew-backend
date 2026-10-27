package org.sopt.makers.crew.main.dining.v2.service;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningCloseGuideTarget;
import org.sopt.makers.crew.main.dining.v2.dto.notification.DiningReminderTarget;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.entity.dining.DiningRoundRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * StubTime 기준 현재 시각: 2024.04.24 00:00 (오늘 2024.04.24, 내일 2024.04.25)
 */
@IntegratedTest
@Import(CaffeineTestConfig.class)
class DiningReminderServiceTest {

	private static final String TODAY = "2024.04.24";
	private static final String TOMORROW = "2024.04.25";

	@Autowired
	private DiningReminderService diningReminderService;
	@Autowired
	private DiningV2Service diningV2Service;
	@Autowired
	private DiningRoundRepository diningRoundRepository;
	@Autowired
	private UserRepository userRepository;

	private User leader;
	private User member;

	@BeforeEach
	void setUp() {
		leader = userRepository.save(user("개설자", 1));
		member = userRepository.save(user("참석자", 2));
	}

	@Test
	@DisplayName("마감 안내는 오늘 회식 중 종료되지 않고 마감하지 않은 회차가 있는 회식의 개설자만 대상이다.")
	void closeGuide_targetsTodayDiningsWithOpenRound() {
		Integer open = createDining("미마감 회식", TODAY);
		Integer partlyClosed = createDining("일부 마감 회식", TODAY);
		diningV2Service.createRound(partlyClosed, roundBody(), leader.getId());
		diningV2Service.closeRound(partlyClosed, firstRoundId(partlyClosed), leader.getId());
		Integer allClosed = createDining("전체 마감 회식", TODAY);
		diningV2Service.closeRound(allClosed, firstRoundId(allClosed), leader.getId());
		Integer ended = createDining("종료된 회식", TODAY);
		diningV2Service.endDining(ended, leader.getId());
		createDining("내일 회식", TOMORROW);

		List<DiningCloseGuideTarget> targets = diningReminderService.findCloseGuideTargets();

		assertThat(targets).extracting(DiningCloseGuideTarget::meetingId)
			.contains(open, partlyClosed)
			.doesNotContain(allClosed, ended);
		assertThat(targets).filteredOn(target -> target.meetingId().equals(open))
			.singleElement()
			.satisfies(target -> {
				assertThat(target.diningTitle()).isEqualTo("미마감 회식");
				assertThat(target.leaderUserId()).isEqualTo(leader.getId());
			});
	}

	@Test
	@DisplayName("전날 리마인드는 내일 회식 중 종료되지 않은 회식의 전체 회차 참석자를 개설자 포함, 중복 없이 담는다.")
	void reminder_targetsAllAttendeesOfTomorrowDinings() {
		Integer tomorrow = createDining("내일 회식", TOMORROW);
		Integer second = diningV2Service.createRound(tomorrow, roundBody(), leader.getId()).roundId();
		diningV2Service.applyAttendance(tomorrow, firstRoundId(tomorrow), member.getId());
		diningV2Service.applyAttendance(tomorrow, second, member.getId());
		Integer ended = createDining("종료된 내일 회식", TOMORROW);
		diningV2Service.endDining(ended, leader.getId());
		Integer today = createDining("오늘 회식", TODAY);

		List<DiningReminderTarget> targets = diningReminderService.findReminderTargets();

		assertThat(targets).extracting(DiningReminderTarget::meetingId)
			.contains(tomorrow)
			.doesNotContain(ended, today);
		assertThat(targets).filteredOn(target -> target.meetingId().equals(tomorrow))
			.singleElement()
			.satisfies(target -> {
				assertThat(target.diningTitle()).isEqualTo("내일 회식");
				assertThat(target.receiverUserIds()).containsExactlyInAnyOrder(leader.getId(), member.getId());
			});
	}

	private Integer createDining(String title, String date) {
		return diningV2Service.createDining(new DiningV2CreateAndUpdateDiningBodyDto(title, null, null, null, date,
			"18:00", new MeetingJoinablePart[] {MeetingJoinablePart.SERVER}, List.of()), leader.getId()).meetingId();
	}

	private Integer firstRoundId(Integer meetingId) {
		return diningRoundRepository.findAllByMeetingIdOrderByIdAsc(meetingId).get(0).getId();
	}

	private static DiningV2CreateAndUpdateRoundBodyDto roundBody() {
		return new DiningV2CreateAndUpdateRoundBodyDto("2차", null, null, "21:00");
	}

	private static User user(String name, Integer orgId) {
		return User.builder().name(name).orgId(orgId).activities(List.of(new UserActivityVO("서버", 35)))
			.profileImage("profile.jpg").phone("010-1234-5678").build();
	}
}
