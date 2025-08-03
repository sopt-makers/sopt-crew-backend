package org.sopt.makers.crew.main.user.v2;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.*;
import static org.assertj.core.groups.Tuple.tuple;
import static org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.user.v2.dto.response.ApplyV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.MeetingV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAllUserDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetAppliedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetCreatedMeetingByUserResponseDto;
import org.sopt.makers.crew.main.user.v2.dto.response.UserV2GetInterestedKeywordsResponseDto;
import org.sopt.makers.crew.main.user.v2.service.UserV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@IntegratedTest
public class UserServiceTest {

	@Autowired
	private UserV2Service userV2Service;

	@Autowired
	private UserRepository userRepository;

	@Nested
	class 관심있는_키워드_관련_테스트 {
		@Test
		void 관심있는_키워드_등록_정상() {

			User user = UserFixture.createStaticUser();
			User save = userRepository.save(user);

			userV2Service.updateInterestedKeywords(save.getId(), List.of("운동"));

			List<MeetingKeywordType> interestedKeywords = user.getInterestedKeywords();
			Assertions.assertThat(interestedKeywords)
				.hasSize(1)
				.contains(MeetingKeywordType.EXERCISE);
		}

		@Test
		void 관심있는_키워드_없는경우도_정상처리() {
			User user = UserFixture.createStaticUser();
			User save = userRepository.save(user);

			userV2Service.updateInterestedKeywords(save.getId(), Collections.emptyList());

			List<MeetingKeywordType> interestedKeywords = user.getInterestedKeywords();
			Assertions.assertThat(interestedKeywords)
				.hasSize(0);
		}

		@Test
		void 관심있는_키워드_조회() {
			User user = UserFixture.createStaticUser();
			User saveUser = userRepository.save(user);

			userV2Service.updateInterestedKeywords(saveUser.getId(), List.of("운동", "먹방"));

			UserV2GetInterestedKeywordsResponseDto interestedKeywords = userV2Service.getInterestedKeywords(
				saveUser.getId());

			Assertions.assertThat(interestedKeywords.keywords())
				.hasSize(2)
				.contains(MeetingKeywordType.EXERCISE.getValue(), MeetingKeywordType.FOOD.getValue());

		}
	}

	@Nested
	class 전체_사용자_조회 {
		@Test
		void 멘션_사용자_조회() {
			// given
			User user1 = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 34)))
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User user2 = User.builder()
				.name("김철수")
				.orgId(2)
				.activities(List.of(new UserActivityVO("iOS", 30), new UserActivityVO("안드로이드", 33)))
				.profileImage("image-url2")
				.phone("010-1111-2222")
				.build();
			userRepository.saveAll(List.of(user1, user2));

			// when
			List<UserV2GetAllUserDto> allMentionUsers = userV2Service.getAllUser();

			// then
			assertThat(allMentionUsers).hasSize(2);
			assertThat(allMentionUsers.get(0))
				.extracting("userName", "recentPart", "recentGeneration", "profileImageUrl")
				.containsExactly("홍길동", "iOS", 34, "image-url1");
			assertThat(allMentionUsers.get(1))
				.extracting("userName", "recentPart", "recentGeneration", "profileImageUrl")
				.containsExactly("김철수", "안드로이드", 33, "image-url2");

		}

		@Test
		void 멘션_사용자_조회시_db에_null_저장된_경우() {
			// given
			User user1 = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(null)
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User user2 = User.builder()
				.name("김철수")
				.orgId(2)
				.activities(List.of(new UserActivityVO("iOS", 30), new UserActivityVO("안드로이드", 33)))
				.profileImage("image-url2")
				.phone("010-1111-2222")
				.build();
			userRepository.saveAll(List.of(user1, user2));

			// when
			List<UserV2GetAllUserDto> allMentionUsers = userV2Service.getAllUser();

			// then
			assertThat(allMentionUsers).hasSize(1);
			assertThat(allMentionUsers.get(0))
				.extracting("userName", "recentPart", "recentGeneration", "profileImageUrl")
				.containsExactly("김철수", "안드로이드", 33, "image-url2");
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/user-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 내가_신청한_모임_조회 {

		@Test
		@DisplayName("내가 신청한 모임 목록을 가장 최근에 신청한 순으로 조회할 수 있다.")
		void getAppliedMeetingByUser_userApplyMeetings() {
			// given
			Integer userId = 2;

			// when
			UserV2GetAppliedMeetingByUserResponseDto responseDto = userV2Service.getAppliedMeetingByUser(
				userId);

			// then
			Assertions.assertThat(responseDto.apply()).hasSize(4);

			List<ApplyV2GetAppliedMeetingByUserResponseDto> applyResponseDto = responseDto.apply();

			Assertions.assertThat(applyResponseDto)
				.extracting("status", "meeting.title", "meeting.status")
				.containsExactly(
					tuple(1, "세미나 구합니다 - 신청후", 2),
					tuple(0, "스터디 구합니다 - 신청후", 2),
					tuple(0, "스터디 구합니다 - 신청전", 0),
					tuple(0, "스터디 구합니다1", 1)
				);
		}

		@Test
		@DisplayName("내가 신청한 모임 목록에서 모임장이나 공동모임장이 될 수 없다.")
		void getAppliedMeetingByUser_notLeaderOrCoLeader() {
			// given
			Integer userId = 2;

			// when
			UserV2GetAppliedMeetingByUserResponseDto responseDto = userV2Service.getAppliedMeetingByUser(
				userId);

			// then
			Assertions.assertThat(responseDto.apply()).hasSize(4);

			List<ApplyV2GetAppliedMeetingByUserResponseDto> applyResponseDto = responseDto.apply();

			Assertions.assertThat(applyResponseDto)
				.extracting("status", "meeting.isCoLeader", "meeting.title", "meeting.status")
				.containsExactly(
					tuple(1, false, "세미나 구합니다 - 신청후", 2),
					tuple(0, false, "스터디 구합니다 - 신청후", 2),
					tuple(0, false, "스터디 구합니다 - 신청전", 0),
					tuple(0, false, "스터디 구합니다1", 1)
				);
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/user-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 내가_만든_모임_조회 {

		@Test
		@DisplayName("내가 만든 모임 목록을 조회할 수 있다.")
		void getCreatedMeetingByUser_meetingMadeByUser() {
			// given
			Integer userId = 4;

			// when
			UserV2GetCreatedMeetingByUserResponseDto responseDto = userV2Service.getCreatedMeetingByUser(userId);

			// then
			Assertions.assertThat(responseDto.meetings()).hasSize(3);

			List<MeetingV2GetCreatedMeetingByUserResponseDto> meetings = responseDto.meetings();

			Assertions.assertThat(meetings)
				.extracting("title", "targetActiveGeneration", "joinableParts", "category",
					"canJoinOnlyActiveGeneration", "status", "isCoLeader", "isMentorNeeded",
					"startDate", "endDate", "mStartDate", "mEndDate",
					"capacity", "approvedCount", "welcomeMessageTypes", "meetingKeywordTypes")
				.containsExactly(
					tuple("세미나 구합니다 - 신청후", null, new MeetingJoinablePart[] {WEB, IOS}, "세미나",
						false, 2, false, false,
						LocalDateTime.of(2024, 4, 22, 0, 0, 0), LocalDateTime.of(2024, 4, 22, 23, 59, 59),
						LocalDateTime.of(2024, 5, 29, 0, 0, 0), LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						13, 1, List.of("숙련자 환영", "OB 환영"), List.of("기타")),
					tuple("스터디 구합니다 - 신청후", null, new MeetingJoinablePart[] {PM, SERVER}, "스터디",
						false, 2, false, false,
						LocalDateTime.of(2024, 4, 22, 0, 0, 0), LocalDateTime.of(2024, 4, 22, 23, 59, 59),
						LocalDateTime.of(2024, 5, 29, 0, 0, 0), LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						10, 0, List.of("입문자 환영", "OB 환영"), List.of("기타")),
					tuple("스터디 구합니다 - 신청전", null, new MeetingJoinablePart[] {PM, SERVER}, "스터디",
						false, 0, false, false,
						LocalDateTime.of(2024, 4, 25, 0, 0, 0), LocalDateTime.of(2024, 5, 24, 23, 59, 59),
						LocalDateTime.of(2024, 5, 29, 0, 0, 0), LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						10, 0, List.of("초면 환영", "OB 환영"), List.of("기타"))
				);
		}

		@Test
		@DisplayName("공동 모임장인 모임도 내가 만든 모임 목록에 조회된다.")
		void getCreatedMeetingByUser_coLeaderMeeting() {
			// given
			Integer userId = 5;

			// when
			UserV2GetCreatedMeetingByUserResponseDto responseDto = userV2Service.getCreatedMeetingByUser(userId);

			// then
			Assertions.assertThat(responseDto.meetings()).hasSize(1);

			List<MeetingV2GetCreatedMeetingByUserResponseDto> meetings = responseDto.meetings();

			Assertions.assertThat(meetings)
				.extracting("title", "targetActiveGeneration", "joinableParts", "category",
					"canJoinOnlyActiveGeneration", "status", "isCoLeader", "isMentorNeeded",
					"startDate", "endDate", "mStartDate", "mEndDate",
					"capacity", "approvedCount", "welcomeMessageTypes", "meetingKeywordTypes")
				.containsExactly(
					tuple("세미나 구합니다 - 신청후", null, new MeetingJoinablePart[] {WEB, IOS}, "세미나",
						false, 2, true, false,
						LocalDateTime.of(2024, 4, 22, 0, 0, 0), LocalDateTime.of(2024, 4, 22, 23, 59, 59),
						LocalDateTime.of(2024, 5, 29, 0, 0, 0), LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						13, 1, List.of("숙련자 환영", "OB 환영"), List.of("기타"))
				);
		}
	}
}
