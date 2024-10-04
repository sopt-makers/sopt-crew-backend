package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.groups.Tuple.*;
import static org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart.*;
import static org.sopt.makers.crew.main.global.constant.CrewConst.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.ApplySearchRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.global.dto.MeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@IntegratedTest
public class MeetingV2ServiceTest {

	@Autowired
	private MeetingV2Service meetingV2Service;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Qualifier("applySearchRepositoryImpl")
	private ApplySearchRepository applySearchRepository;

	@Autowired
	private ApplyRepository applyRepository;

	@Nested
	class 모임_생성 {
		@ParameterizedTest
		@ValueSource(booleans = {true, false}) // true와 false 두 가지 경우에 대해 테스트
		@DisplayName("모임 생성 성공 시, 생성된 모임 번호가 반환된다.")
		void normal_createMeeting_meetingId(boolean canJoinOnlyActiveGeneration) {
			// given
			User user = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 34)))
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User savedUser = userRepository.save(user);

			// 모임 이미지 리스트
			List<String> files = Arrays.asList("https://example.com/image1.jpg");

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS};

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto("알고보면 쓸데있는 개발 프로세스", // title
				files, // files (모임 이미지 리스트)
				"스터디", // category
				"2024.10.01", // startDate (모집 시작 날짜)
				"2024.10.15", // endDate (모집 끝 날짜)
				10, // capacity (모집 인원)
				"백엔드 개발에 관심 있는 사람들을 위한 스터디입니다.", // desc (모집 정보)
				"매주 온라인으로 진행되며, 발표와 토론이 포함됩니다.", // processDesc (진행 방식 소개)
				"2024.10.16", // mStartDate (모임 활동 시작 날짜)
				"2024.12.30", // mEndDate (모임 활동 종료 날짜)
				"5년차 백엔드 개발자입니다.", // leaderDesc (개설자 소개)
				"준비물은 노트북과 열정입니다.", // note (유의할 사항)
				false, // isMentorNeeded (멘토 필요 여부)
				canJoinOnlyActiveGeneration, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinableParts // joinableParts (대상 파트 목록)
			);

			// when
			MeetingV2CreateMeetingResponseDto responseDto = meetingV2Service.createMeeting(meetingDto,
				savedUser.getId());
			Meeting foundMeeting = meetingRepository.findByIdOrThrow(responseDto.getMeetingId());

			// then
			Assertions.assertThat(foundMeeting.getId()).isEqualTo(responseDto.getMeetingId());

			Assertions.assertThat(foundMeeting)
				.isNotNull()
				.extracting("user", "userId", "title", "category", "startDate", "endDate", "capacity", "desc",
					"processDesc", "mStartDate", "mEndDate", "leaderDesc", "note", "isMentorNeeded",
					"canJoinOnlyActiveGeneration", "createdGeneration", "targetActiveGeneration", "joinableParts")
				.containsExactly(savedUser,  // user 필드
					savedUser.getId(),  // userId 필드
					"알고보면 쓸데있는 개발 프로세스",  // title 필드
					MeetingCategory.STUDY,  // category 필드
					LocalDateTime.of(2024, 10, 1, 0, 0, 0),  // startDate 필드
					LocalDateTime.of(2024, 10, 15, 23, 59, 59),  // endDate 필드
					10,  // capacity 필드
					"백엔드 개발에 관심 있는 사람들을 위한 스터디입니다.",  // desc 필드
					"매주 온라인으로 진행되며, 발표와 토론이 포함됩니다.",  // processDesc 필드
					LocalDateTime.of(2024, 10, 16, 0, 0, 0),  // mStartDate 필드
					LocalDateTime.of(2024, 12, 30, 23, 59, 59),  // mEndDate 필드
					"5년차 백엔드 개발자입니다.",  // leaderDesc 필드
					"준비물은 노트북과 열정입니다.",  // note 필드
					false,  // isMentorNeeded 필드
					canJoinOnlyActiveGeneration,  // canJoinOnlyActiveGeneration 필드
					ACTIVE_GENERATION,  // createdGeneration 필드
					canJoinOnlyActiveGeneration ? ACTIVE_GENERATION : null,  // targetActiveGeneration 필드
					new MeetingJoinablePart[] {MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS}  // joinableParts 필드
				);

			Assertions.assertThat(foundMeeting.getImageURL())
				.hasSize(1)
				.extracting("url")
				.containsExactly("https://example.com/image1.jpg");
		}

		@Test
		@DisplayName("모임 개설자의 활동기수 정보가 없을 경우, 예외가 발생한다.")
		void userHasNotActivities_createMeeting_exception() {
			// given
			User user = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(null)
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User savedUser = userRepository.save(user);

			// 모임 이미지 리스트
			List<String> files = Arrays.asList(
				"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df");

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {MeetingJoinablePart.ANDROID, MeetingJoinablePart.IOS};

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto("알고보면 쓸데있는 개발 프로세스", // title
				files, // files (모임 이미지 리스트)
				"스터디", // category
				"2022.10.08", // startDate (모집 시작 날짜)
				"2022.10.09", // endDate (모집 끝 날짜)
				5, // capacity (모집 인원)
				"api 가 터졌다고? 깃이 터졌다고?", // desc (모집 정보)
				"소요 시간 : 1시간 예상", // processDesc (진행 방식 소개)
				"2022.10.29", // mStartDate (모임 활동 시작 날짜)
				"2022.10.30", // mEndDate (모임 활동 종료 날짜)
				"안녕하세요 기획 파트 000입니다", // leaderDesc (개설자 소개)
				"유의할 사항", // note (유의할 사항)
				false, // isMentorNeeded (멘토 필요 여부)
				false, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinableParts // joinableParts (대상 파트 목록)
			);

			// when, then
			Assertions.assertThatThrownBy(() -> meetingV2Service.createMeeting(meetingDto, savedUser.getId()))
				.hasMessageContaining(VALIDATION_EXCEPTION.getErrorCode());
		}

		@Test
		@DisplayName("모임 생성할 때, 대상 파트 목록이 빈값이면 예외가 발생한다.")
		void isJoinalbePartsEmpty_createMeeting_exception() {
			// given
			User user = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(null)
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User savedUser = userRepository.save(user);

			// 모임 이미지 리스트
			List<String> files = Arrays.asList(
				"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df");

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = null;

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto("알고보면 쓸데있는 개발 프로세스", // title
				files, // files (모임 이미지 리스트)
				"스터디", // category
				"2022.10.08", // startDate (모집 시작 날짜)
				"2022.10.09", // endDate (모집 끝 날짜)
				5, // capacity (모집 인원)
				"api 가 터졌다고? 깃이 터졌다고?", // desc (모집 정보)
				"소요 시간 : 1시간 예상", // processDesc (진행 방식 소개)
				"2022.10.29", // mStartDate (모임 활동 시작 날짜)
				"2022.10.30", // mEndDate (모임 활동 종료 날짜)
				"안녕하세요 기획 파트 000입니다", // leaderDesc (개설자 소개)
				"유의할 사항", // note (유의할 사항)
				false, // isMentorNeeded (멘토 필요 여부)
				false, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinableParts // joinableParts (대상 파트 목록)
			);

			// when, then
			Assertions.assertThatThrownBy(() -> meetingV2Service.createMeeting(meetingDto, savedUser.getId()))
				.hasMessageContaining(VALIDATION_EXCEPTION.getErrorCode());
		}

		@Test
		@DisplayName("모임 생성할 때, 이미지가 없을 경우 예외가 발생한다.")
		void isImageFileEmpty_createMeeting_exception() {
			// given
			User user = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(null)
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User savedUser = userRepository.save(user);

			// 모임 이미지 리스트
			List<String> files = null;

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {MeetingJoinablePart.ANDROID, MeetingJoinablePart.IOS};

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto("알고보면 쓸데있는 개발 프로세스", // title
				files, // files (모임 이미지 리스트)
				"스터디", // category
				"2022.10.08", // startDate (모집 시작 날짜)
				"2022.10.09", // endDate (모집 끝 날짜)
				5, // capacity (모집 인원)
				"api 가 터졌다고? 깃이 터졌다고?", // desc (모집 정보)
				"소요 시간 : 1시간 예상", // processDesc (진행 방식 소개)
				"2022.10.29", // mStartDate (모임 활동 시작 날짜)
				"2022.10.30", // mEndDate (모임 활동 종료 날짜)
				"안녕하세요 기획 파트 000입니다", // leaderDesc (개설자 소개)
				"유의할 사항", // note (유의할 사항)
				false, // isMentorNeeded (멘토 필요 여부)
				false, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinableParts // joinableParts (대상 파트 목록)
			);

			// when, then
			Assertions.assertThatThrownBy(() -> meetingV2Service.createMeeting(meetingDto, savedUser.getId()))
				.hasMessageContaining(VALIDATION_EXCEPTION.getErrorCode());
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 모임_전체_조회 {
		@Test
		@DisplayName("활동기수 아닌 경우를 포함하여 모임 전체 조회 시, 모임 목록을 반환한다.")
		void normal_getMeetings_success() {
			// given
			int page = 1;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();
			List<MeetingCreatorDto> meetingCreatorDtos = meetings.stream().map(MeetingResponseDto::getUser).toList();

			// then
			Assertions.assertThat(meetings)
				.extracting("id", "title", "category", "canJoinOnlyActiveGeneration", "mStartDate", "mEndDate",
					"capacity", "isMentorNeeded", "targetActiveGeneration", "joinableParts", "status", "appliedCount")
				.containsExactly(tuple(2, "스터디 구합니다 - 신청전", "스터디", false, LocalDateTime.of(2024, 5, 29, 0, 0),
					LocalDateTime.of(2024, 5, 31, 23, 59, 59), 10, false, null, new MeetingJoinablePart[] {PM, SERVER},
					0, 0), tuple(1, "스터디 구합니다1", "행사", true, LocalDateTime.of(2024, 5, 29, 0, 0),
					LocalDateTime.of(2024, 5, 31, 23, 59, 59), 10, true, 35, new MeetingJoinablePart[] {PM, SERVER}, 1,
					2)

				);

			Assertions.assertThat(meetingCreatorDtos)
				.extracting("id", "name", "orgId", "profileImage", "activities", "phone")
				.containsExactly(tuple(5, "모임개설자2", 1005, "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)), "010-6666-6666"),
					tuple(1, "모임개설자", 1001, "profile1.jpg",
						List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 32)), "010-1234-5678")

				);
		}
	}

	@Nested
	class 지원자_참여자_조회 {
		@Test
		@DisplayName("지원자/참여자를 조회할 경우, 지원자 목록 10개를 반환한다.")
		void returns_10_applicants_when_querying_for_applicants_participants() {
			// given
			User user = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(user);

			Meeting meeting = Meeting.builder()
				.user(user)
				.userId(user.getId())
				.title("지원자/참여자 조회 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 10, 24, 0, 0, 0))
				.endDate(LocalDateTime.of(2024, 10, 29, 23, 59, 59))
				.capacity(20)
				.desc("지원자/참여자 조회 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2024, 11, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 12, 24, 0, 0, 0))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build();

			meetingRepository.save(meeting);

			for (int i = 1; i <= 6; i++) {
				User applicant1 = User.builder()
					.name("지원자 " + i)
					.orgId(i + 1)
					.activities(List.of(new UserActivityVO("안드로이드", 35)))
					.profileImage("applicantProfile" + i + ".jpg")
					.phone("010-1234-56" + (78 + i))
					.build();

				userRepository.save(applicant1);

				Apply apply1 = Apply.builder()
					.type(EnApplyType.APPLY)
					.meeting(meeting)
					.meetingId(meeting.getId())
					.user(applicant1)
					.userId(100 + i)
					.content("지원 동기 " + i)
					.build();

				apply1.updateApplyStatus(EnApplyStatus.APPROVE);

				applyRepository.save(apply1);
			}

			for (int i = 7; i <= 12; i++) {
				User applicant2 = User.builder()
					.name("지원자 " + i)
					.orgId(i + 1)
					.activities(List.of(new UserActivityVO("기획", 35)))
					.profileImage("applicantProfile" + i + ".jpg")
					.phone("010-1234-56" + (78 + i))
					.build();

				userRepository.save(applicant2);

				Apply apply2 = Apply.builder()
					.type(EnApplyType.APPLY)
					.meeting(meeting)
					.meetingId(meeting.getId())
					.user(applicant2)
					.userId(100 + i)
					.content("지원 동기 " + i)
					.build();

				applyRepository.save(apply2);
			}

			Integer meetingId = meeting.getId();
			Integer userId = user.getId();

			MeetingGetAppliesQueryDto queryCommand = MeetingGetAppliesQueryDto.builder()
				.page(1)
				.take(10)
				.status(null)
				.date("desc")
				.build();

			// When: 서비스에서 지원자/참여자 리스트를 조회
			MeetingGetApplyListResponseDto responseDto = meetingV2Service.findApplyList(queryCommand, meetingId,
				userId);

			// Then: 조회된 결과를 검증
			Assertions.assertThat(responseDto).isNotNull();
			Assertions.assertThat(responseDto.getApply()).isNotEmpty();  // 신청 목록이 비어 있지 않아야 함
			Assertions.assertThat(responseDto.getMeta().getPage()).isEqualTo(1);  // 페이지 정보가 예상대로인지 검증
			Assertions.assertThat(responseDto.getMeta().getTake()).isEqualTo(10);  // 한 페이지당 지원자 수가 10명인지 검증

			// 추가 검증: 실제 데이터와 일치하는지 확인
			PageRequest pageable = PageRequest.of(queryCommand.getPage() - 1, queryCommand.getTake());
			Page<ApplyInfoDto> applyList = applySearchRepository.findApplyList(queryCommand, pageable, meetingId,
				meeting.getUserId(), userId);

			// 총 지원자 수 비교 -> getMeta().getItemCount() 사용
			Assertions.assertThat(applyList.getTotalElements()).isEqualTo(responseDto.getMeta().getItemCount());
			// 지원자 목록 비교
			Assertions.assertThat(applyList.getContent()).containsAll(responseDto.getApply());
		}
	}
}
