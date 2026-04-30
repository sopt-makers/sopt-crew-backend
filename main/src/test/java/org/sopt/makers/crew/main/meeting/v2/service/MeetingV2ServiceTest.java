package org.sopt.makers.crew.main.meeting.v2.service;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart.*;
import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.makers.crew.main.entity.apply.Apply;
import org.sopt.makers.crew.main.entity.apply.ApplyRepository;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.EnMeetingStatus;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.external.CaffeineTestConfig;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.global.dto.MeetingResponseDto;
import org.sopt.makers.crew.main.global.exception.BadRequestException;
import org.sopt.makers.crew.main.global.exception.ForbiddenException;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.sopt.makers.crew.main.global.util.ActiveGenerationProvider;
import org.sopt.makers.crew.main.meeting.v2.dto.ApplyMapper;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingGetAppliesQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.query.MeetingV2GetAllMeetingQueryDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.ApplyV2UpdateStatusBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2ApplyMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2UpdateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.ApplyWholeInfoDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingGetApplyListResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2ApplyMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetAllMeetingDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingByIdResponseDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2GetMeetingPartMembersResponseDto;
import org.sopt.makers.crew.main.tag.v2.service.TagV2Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

@IntegratedTest
@Import(CaffeineTestConfig.class)
public class MeetingV2ServiceTest {

	@Autowired
	private MeetingV2Service meetingV2Service;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private CoLeaderRepository coLeaderRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplyRepository applyRepository;

	@Autowired
	private ApplyMapper applyMapper;

	@Autowired
	private ActiveGenerationProvider activeGenerationProvide;

	@Autowired
	private TagV2Service tagV2Service;

	private Meeting createMeetingFixture(Integer index, User user) {

		List<ImageUrlVO> imageUrlVOList = Arrays.asList(
			new ImageUrlVO(1, "https://example.com/image1.png"),
			new ImageUrlVO(2, "https://example.com/image2.png")
		);

		return Meeting.builder()
			.userId(user.getId())
			.user(user)
			.title("Weekly Coding Meetup" + index)
			.category(MeetingCategory.STUDY)
			.imageURL(imageUrlVOList)
			.startDate(LocalDateTime.of(2024, 5, 29, 0, 0))
			.endDate(LocalDateTime.of(2024, 5, 31, 0, 0))
			.capacity(50)
			.desc("Let's gather and improve our coding skills.")
			.processDesc("Online meeting via Zoom.")
			.mStartDate(LocalDateTime.of(2024, 5, 29, 0, 0))  // 모임 시작일: 4일 후
			.mEndDate(LocalDateTime.of(2024, 5, 31, 0, 0))    // 모임 종료일: 5일 후
			.leaderDesc("John Doe is an experienced software engineer.")
			.note("Bring your best coding questions!")
			.isMentorNeeded(true)
			.canJoinOnlyActiveGeneration(false)
			.createdGeneration(34)
			.joinableParts(new MeetingJoinablePart[] {WEB, SERVER})
			.build();
	}

	private MeetingJoinInfo createMeetingJoinInfo() {
		return new MeetingJoinInfo(MeetingType.ONLINE_OFFLINE, MeetingFrequency.STEADY);
	}

	private void saveApplyUser(Integer meetingId, Integer userId, String name, String part) {
		saveApplyUser(meetingId, userId, name, part, 38);
	}

	private void saveApplyUser(Integer meetingId, Integer userId, String name, String part, int generation) {
		User user = userRepository.save(User.builder()
			.name(name)
			.orgId(userId)
			.activities(List.of(new UserActivityVO(part, generation)))
			.profileImage("profile.jpg")
			.phone("010-0000-0000")
			.build());

		Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
		Apply apply = Apply.builder()
			.type(EnApplyType.APPLY)
			.meeting(meeting)
			.meetingId(meetingId)
			.user(user)
			.userId(user.getId())
			.content(part + " 신청합니다.")
			.build();
		applyRepository.save(apply);
	}

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
			List<String> files = Arrays.asList(
				"https://example.com/image1.jpg"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.SERVER,
				MeetingJoinablePart.IOS
			};

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"백엔드 실전 설계부터 배포까지", // subTitle
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
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				null, // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
			);

			// when
			MeetingV2CreateMeetingResponseDto responseDto = meetingV2Service.createMeeting(meetingDto,
				savedUser.getId());
			Meeting foundMeeting = meetingRepository.findByIdOrThrow(responseDto.getMeetingId());

			// then
			Assertions.assertThat(foundMeeting.getId()).isEqualTo(responseDto.getMeetingId());

			Assertions.assertThat(foundMeeting)
				.isNotNull()
				.extracting(
					"user", "userId", "title", "subTitle", "category", "startDate", "endDate", "capacity", "desc",
					"processDesc", "mStartDate", "mEndDate", "leaderDesc", "note", "isMentorNeeded",
					"canJoinOnlyActiveGeneration", "joinInfo", "createdGeneration", "targetActiveGeneration", "joinableParts"
				)
				.containsExactly(
					savedUser,  // user 필드
					savedUser.getId(),  // userId 필드
					"알고보면 쓸데있는 개발 프로세스",  // title 필드
					"백엔드 실전 설계부터 배포까지", // subTitle 필드
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
					joinInfo, // joinInfo 필드
					activeGenerationProvide.getActiveGeneration(),  // createdGeneration 필드
					canJoinOnlyActiveGeneration ? activeGenerationProvide.getActiveGeneration() : null,
					// targetActiveGeneration 필드
					new MeetingJoinablePart[] {MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS} // joinableParts 필드
				);

			Assertions.assertThat(foundMeeting.getImageURL())
				.hasSize(1)
				.extracting("url")
				.containsExactly(
					"https://example.com/image1.jpg"
				);
		}

		@Test
		@DisplayName("공동 모임장을 포함하여 모임 생성 성공 시, 생성된 모임 번호가 반환되며 공동 모임장으로 저장된다.")
		void coLeader_createMeeting_meetingId() {
			// given
			User user = User.builder()
				.name("홍길동")
				.orgId(1)
				.activities(List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 34)))
				.profileImage("image-url1")
				.phone("010-1234-5678")
				.build();
			User savedUser = userRepository.save(user);

			User coLeader1 = User.builder()
				.name("공동모임장1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("서버", 34), new UserActivityVO("iOS", 34)))
				.profileImage("image-url11")
				.phone("010-1234-5678")
				.build();
			User savedJointLeader1 = userRepository.save(coLeader1);

			User coLeader2 = User.builder()
				.name("공동모임장2")
				.orgId(3)
				.activities(List.of(new UserActivityVO("서버", 34), new UserActivityVO("iOS", 34)))
				.profileImage("image-url12")
				.phone("010-1234-5678")
				.build();
			User savedJointLeader2 = userRepository.save(coLeader2);

			// 모임 이미지 리스트
			List<String> files = Arrays.asList(
				"https://example.com/image1.jpg"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.SERVER,
				MeetingJoinablePart.IOS
			};

			// 환영 태그 목록
			List<String> welcomeMessageTypes = List.of("YB 환영", "OB 환영");

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"알고보면 쓸데있는 개발 프로세스", // subTitle
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
				true, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				List.of(savedJointLeader1.getId(), savedJointLeader2.getId()), // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
			);

			// when
			MeetingV2CreateMeetingResponseDto responseDto = meetingV2Service.createMeeting(meetingDto,
				savedUser.getId());

			// then
			Meeting foundMeeting = meetingRepository.findByIdOrThrow(responseDto.getMeetingId());
			List<CoLeader> coLeaders = coLeaderRepository.findAllByMeetingId(foundMeeting.getId());

			Assertions.assertThat(foundMeeting.getId()).isEqualTo(responseDto.getMeetingId());

			Assertions.assertThat(foundMeeting).isNotNull();
			Assertions.assertThat(coLeaders)
				.hasSize(2)
				.extracting("user.name", "meeting.id")
				.containsExactly(
					tuple("공동모임장1", foundMeeting.getId()),
					tuple("공동모임장2", foundMeeting.getId())
				);
		}

		@Test
		@DisplayName("존재하지 않는 공동 모임장 id를 요청할 경우, 예외가 발생한다.")
		void notPresentCoLeaderId_createMeeting_throwException() {
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
			List<String> files = Arrays.asList(
				"https://example.com/image1.jpg"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.SERVER,
				MeetingJoinablePart.IOS
			};

			// 환영 태그 목록
			List<String> welcomeMessageTypes = List.of("YB 환영", "OB 환영");

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"알고보면 쓸데있는 개발 프로세스", // subTitle
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
				true, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				List.of(0, Integer.MAX_VALUE), // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
			);

			// when, then
			Assertions.assertThatThrownBy(() -> meetingV2Service.createMeeting((meetingDto),
					savedUser.getId()))
				.isInstanceOf(NotFoundException.class)
				.hasMessageContaining(NOT_FOUND_USER.getErrorCode());

		}

		@Test
		@DisplayName("모임장은 공동모임장이 될 수 없다.")
		void setLeaderCoLeader_createMeeting_throwException() {
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
			List<String> files = Arrays.asList(
				"https://example.com/image1.jpg"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.SERVER,
				MeetingJoinablePart.IOS
			};

			// 환영 태그 목록
			List<String> welcomeMessageTypes = List.of("YB 환영", "OB 환영");

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"알고보면 쓸데있는 개발 프로세스", // subTitle
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
				true, // canJoinOnlyActiveGeneration (활동기수만 지원 가능 여부)
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				List.of(savedUser.getId()), // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
			);

			// when, then
			Assertions.assertThatThrownBy(() -> meetingV2Service.createMeeting((meetingDto),
					savedUser.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(LEADER_CANNOT_BE_CO_LEADER_APPLY.getErrorCode());

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
				"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.ANDROID,
				MeetingJoinablePart.IOS
			};

			// 환영 태그 목록
			List<String> welcomeMessageTypes = List.of("YB 환영", "OB 환영");

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"알고보면 쓸데있는 개발 프로세스", // subTitle
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
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				null, // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
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
				"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = null;

			// 환영 태그 목록
			List<String> welcomeMessageTypes = List.of("YB 환영", "초면 환영");

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"알고보면 쓸데있는 개발 프로세스", // subTitle
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
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				null, // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
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
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.ANDROID,
				MeetingJoinablePart.IOS
			};

			// 환영 태그 목록
			List<String> welcomeMessageTypes = List.of("YB 환영", "초면 환영");

			// 모임 키워드 목록
			List<String> meetingKeywordTypes = List.of("자기계발", "네트워킹");

			MeetingJoinInfo joinInfo = createMeetingJoinInfo();

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
				"알고보면 쓸데있는 개발 프로세스", // subTitle
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
				joinInfo, // joinInfo (참여 정보)
				joinableParts, // joinableParts (대상 파트 목록)
				null, // coLeaders (공동모임장 리스트)
				meetingKeywordTypes // meetingKeywordTypes (모임 키워드 태그 리스트)
			);

			// when, then
			Assertions.assertThatThrownBy(() -> meetingV2Service.createMeeting(meetingDto, savedUser.getId()))
				.hasMessageContaining(VALIDATION_EXCEPTION.getErrorCode());
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/meeting-service-sequence-restart.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
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
			List<MeetingCreatorDto> meetingCreatorDtos = meetings.stream()
				.map(MeetingResponseDto::getUser)
				.toList();

			// then
				Assertions.assertThat(meetings)
					.extracting(
						"title", "subTitle", "category", "canJoinOnlyActiveGeneration",
						"mStartDate", "mEndDate",
						"capacity", "isMentorNeeded", "joinInfo", "targetActiveGeneration",
						"joinableParts", "status", "approvedCount"
					).containsExactly(
						tuple("스터디 구합니다1", "스터디 부제목1", "행사", true,
							LocalDateTime.of(2024, 5, 29, 0, 0),
							LocalDateTime.of(2024, 5, 31, 23, 59, 59),
							10, true, new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY), 35,
							new MeetingJoinablePart[] {PM, SERVER}, 1, 2
						),
						tuple("스터디 구합니다 - 신청전", "스터디 부제목2", "스터디", false,
							LocalDateTime.of(2024, 5, 29, 0, 0),
							LocalDateTime.of(2024, 5, 31, 23, 59, 59),
							10, false, new MeetingJoinInfo(MeetingType.OFFLINE, MeetingFrequency.LIGHT), null,
							new MeetingJoinablePart[] {PM, SERVER}, 0, 0
						),
						tuple("세미나 구합니다 - 신청후", "세미나 부제목4", "세미나", false,
							LocalDateTime.of(2024, 5, 29, 0, 0),
							LocalDateTime.of(2024, 5, 31, 23, 59, 59),
							13, false, new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.LIGHT), null,
							new MeetingJoinablePart[] {WEB, IOS}, 2, 0
						),
						tuple("스터디 구합니다 - 신청후", "스터디 부제목3", "스터디", false,
							LocalDateTime.of(2024, 5, 29, 0, 0),
							LocalDateTime.of(2024, 5, 31, 23, 59, 59),
							10, false, new MeetingJoinInfo(MeetingType.ONLINE_OFFLINE, MeetingFrequency.IMMERSIVE), null,
							new MeetingJoinablePart[] {PM, SERVER}, 2, 0
						)
					);

			Assertions.assertThat(meetingCreatorDtos)
				.extracting("name", "profileImage", "activities", "phone")
				.containsExactly(
					tuple("모임개설자", "profile1.jpg",
						List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 32)),
						"010-1234-5678"),
					tuple("모임개설자2", "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)),
						"010-6666-6666"),
					tuple("모임개설자2", "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)),
						"010-6666-6666"),
					tuple("모임개설자2", "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)),
						"010-6666-6666")
				);
		}

		@Test
		@DisplayName("활동기수에 한하여 모임 전체 조회 시, 활동기수에 해당하는 모임 목록을 반환한다.")
		void getOnlyActiveGenerationMeeting_getMeetings_meetings() {
			// given
			int page = 1;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(true);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();
			List<MeetingCreatorDto> meetingCreatorDtos = meetings.stream()
				.map(MeetingResponseDto::getUser)
				.toList();

			// then
			Assertions.assertThat(meetings)
				.extracting(
					"title", "category", "canJoinOnlyActiveGeneration",
					"mStartDate", "mEndDate",
					"capacity", "isMentorNeeded", "targetActiveGeneration",
					"joinableParts", "status", "approvedCount"
				).containsExactly(
					tuple("스터디 구합니다1", "행사", true,
						LocalDateTime.of(2024, 5, 29, 0, 0),
						LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						10, true, 35,
						new MeetingJoinablePart[] {PM, SERVER}, 1, 2
					)

				);

			Assertions.assertThat(meetingCreatorDtos)
				.extracting("name", "profileImage", "activities", "phone")
				.containsExactly(
					tuple("모임개설자", "profile1.jpg",
						List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 32)),
						"010-1234-5678")

				);
		}

		@Test
		@DisplayName("MeetingCategory 검색 조건에 따라 해당하는 카테고리에 맞는 모임 목록을 반환한다.")
		void getByCategory_getMeetings_meetings() {
			// given
			int page = 1;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);
			List<String> meetingCategories = List.of(MeetingCategory.STUDY.getValue(),
				MeetingCategory.SEMINAR.getValue());
			queryDto.setCategory(meetingCategories);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();
			List<MeetingCreatorDto> meetingCreatorDtos = meetings.stream()
				.map(MeetingResponseDto::getUser)
				.toList();

			// then
			Assertions.assertThat(meetings)
				.extracting(
					"title", "category", "canJoinOnlyActiveGeneration",
					"mStartDate", "mEndDate",
					"capacity", "isMentorNeeded", "targetActiveGeneration",
					"joinableParts", "status", "approvedCount"
				).containsExactly(
					tuple("스터디 구합니다 - 신청전", "스터디", false,
						LocalDateTime.of(2024, 5, 29, 0, 0),
						LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						10, false, null,
						new MeetingJoinablePart[] {PM, SERVER}, 0, 0
					),
					tuple("세미나 구합니다 - 신청후", "세미나", false,
						LocalDateTime.of(2024, 5, 29, 0, 0),
						LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						13, false, null,
						new MeetingJoinablePart[] {WEB, IOS}, 2, 0
					),
					tuple("스터디 구합니다 - 신청후", "스터디", false,
						LocalDateTime.of(2024, 5, 29, 0, 0),
						LocalDateTime.of(2024, 5, 31, 23, 59, 59),
						10, false, null,
						new MeetingJoinablePart[] {PM, SERVER}, 2, 0
					)
				);

			Assertions.assertThat(meetingCreatorDtos)
				.extracting("name", "profileImage", "activities", "phone")
				.containsExactly(
					tuple("모임개설자2", "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)),
						"010-6666-6666"),
					tuple("모임개설자2", "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)),
						"010-6666-6666"),
					tuple("모임개설자2", "profile5.jpg",
						List.of(new UserActivityVO("iOS", 35), new UserActivityVO("안드로이드", 34)),
						"010-6666-6666")

				);
		}

		@Test
		@DisplayName("MeetingStatus 검색 조건에 따라 해당하는 모임 상태에 맞는 모임 목록을 반환한다.")
		void getByStatus_getMeetings_meetings() {
			// given
			int page = 1;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);
			List<String> statues = List.of(String.valueOf(EnMeetingStatus.BEFORE_START.getValue()),
				String.valueOf(EnMeetingStatus.APPLY_ABLE.getValue()));
			queryDto.setStatus(statues);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();

			// then
			Assertions.assertThat(meetings)
				.extracting(
					"title", "category"
				).containsExactly(
					tuple("스터디 구합니다1", "행사"),
					tuple("스터디 구합니다 - 신청전", "스터디")
				);
		}

		@Test
		@DisplayName("joinableParts 검색 조건")
		void getByJoinableParts_getMeetings_meetings() {
			// given
			int page = 1;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);
			queryDto.setJoinableParts(new MeetingJoinablePart[] {IOS});

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();
			// then
			Assertions.assertThat(meetings)
				.extracting(
					"title", "category"
				).containsExactly(
					tuple("세미나 구합니다 - 신청후", "세미나")
				);
		}

		@Test
		@DisplayName("query 검색 조건")
		void getByQuery_getMeetings_meetings() {
			// given
			int page = 1;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);
			queryDto.setQuery("구합니다");

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();
			// then
			Assertions.assertThat(meetings)
				.extracting(
					"title", "category"
				).containsExactly(
					tuple("스터디 구합니다1", "행사"),
					tuple("스터디 구합니다 - 신청전", "스터디"),
					tuple("세미나 구합니다 - 신청후", "세미나"),
					tuple("스터디 구합니다 - 신청후", "스터디")
				);
		}

		@Test
		@DisplayName("검색 결과보다 큰 page를 요청하면 마지막 페이지로 보정하여 반환한다.")
		void pageGreaterThanKeywordSearchResult_getMeetings_lastPage() {
			// given
			int page = 5;
			int take = 12;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);
			queryDto.setKeyword(List.of("기타"));

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);

			// then
			Assertions.assertThat(meetingDto.meetings())
				.extracting(MeetingResponseDto::getTitle)
				.containsExactly("스터디 구합니다1");
			Assertions.assertThat(meetingDto.meta().getPage()).isEqualTo(1);
			Assertions.assertThat(meetingDto.meta().getPageCount()).isEqualTo(1);
			Assertions.assertThat(meetingDto.meta().isHasPreviousPage()).isFalse();
			Assertions.assertThat(meetingDto.meta().isHasNextPage()).isFalse();
		}

		@Test
		@DisplayName("status 필터 결과가 첫 페이지에만 존재하면 두 번째 페이지 요청은 첫 페이지로 보정한다.")
		void outOfRangeStatusPage_getMeetings_firstPage() {
			User user = userRepository.findByIdOrThrow(5);

			for (int i = 0; i < 10; i++) {
				Meeting meeting = createMeetingFixture(i, user);
				meetingRepository.save(meeting);
			}

			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(2, 12);
			queryDto.setIsOnlyActiveGeneration(false);
			queryDto.setStatus(List.of(String.valueOf(EnMeetingStatus.BEFORE_START.getValue())));

			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);

			Assertions.assertThat(meetingDto.meetings()).hasSize(11);
			Assertions.assertThat(meetingDto.meta().getPage()).isEqualTo(1);
			Assertions.assertThat(meetingDto.meta().getItemCount()).isEqualTo(11);
			Assertions.assertThat(meetingDto.meta().getPageCount()).isEqualTo(1);
			Assertions.assertThat(meetingDto.meta().isHasPreviousPage()).isFalse();
			Assertions.assertThat(meetingDto.meta().isHasNextPage()).isFalse();
		}

		@Test
		@DisplayName("페이지네이션에서 page 1일 경우, 11개의 모임목록을 반환한다.")
		void pageIs1_getMeetings_11meetings() {
			// given
			User user = userRepository.findByIdOrThrow(5);

			for (int i = 0; i < 30; i++) {
				Meeting meeting = createMeetingFixture(i, user);
				meetingRepository.save(meeting);
			}
			int page = 1;
			int take = 11;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();

			// then
			Assertions.assertThat(meetings).hasSize(11);
		}

		@Test
		@DisplayName("페이지네이션에서 page 2이상일 경우, 12개의 모임목록을 반환한다.")
		void pageGreaterThen2_getMeetings_12meetings() {
			// given
			User user = userRepository.findByIdOrThrow(5);

			for (int i = 0; i < 30; i++) {
				Meeting meeting = createMeetingFixture(i, user);
				meetingRepository.save(meeting);
			}
			int page = 2;
			int take = 24;
			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(page, take);
			queryDto.setIsOnlyActiveGeneration(false);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);
			List<MeetingResponseDto> meetings = meetingDto.meetings();

			// then
			Assertions.assertThat(meetings).hasSize(12);
		}

		@Test
		@DisplayName("ADVERTISEMENT 페이지네이션의 pageCount는 현재 페이지 크기와 무관하게 계산된다.")
		void advertisementPagination_getMeetings_consistentPageCount() {
			// given
			User user = userRepository.findByIdOrThrow(5);

			for (int i = 0; i < 20; i++) {
				Meeting meeting = createMeetingFixture(i, user);
				meetingRepository.save(meeting);
			}

			MeetingV2GetAllMeetingQueryDto queryDto = new MeetingV2GetAllMeetingQueryDto(2, 12);
			queryDto.setIsOnlyActiveGeneration(false);

			// when
			MeetingV2GetAllMeetingDto meetingDto = meetingV2Service.getMeetings(queryDto);

			// then
			Assertions.assertThat(meetingDto.meetings()).hasSize(12);
			Assertions.assertThat(meetingDto.meta().getPage()).isEqualTo(2);
			Assertions.assertThat(meetingDto.meta().getPageCount()).isEqualTo(3);
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 모임_상세_조회 {

		// ** 중요 ** : meetingStatus 값이 올바른지 검증

		@Test
		@DisplayName("모임장이 상세 조회 시, host에 true 및 모임 데이터를 반환한다.")
		void meetingCreator_getMeetingById_success() {
			// given
			Integer meetingId = 1;
			Integer userId = 1;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto)
				.extracting(
					"id", "userId", "title", "subTitle", "category",
					"startDate", "endDate",
					"capacity", "desc", "processDesc",
					"mStartDate", "mEndDate",
					"leaderDesc", "note", "isMentorNeeded", "canJoinOnlyActiveGeneration", "joinInfo",
					"createdGeneration", "targetActiveGeneration",
					"joinableParts",
					"status", "approvedApplyCount",
					"host", "apply", "approved",
					"user.id", "user.name", "user.profileImage", "user.activities", "user.phone"
				)
				.containsExactly(
					1, 1, "스터디 구합니다1", "스터디 부제목1", "행사",
					LocalDateTime.of(2024, 4, 24, 0, 0, 0),
					LocalDateTime.of(2024, 5, 24, 23, 59, 59),
					10, "스터디 설명입니다.", "스터디 진행방식입니다.",
					LocalDateTime.of(2024, 5, 29, 0, 0, 0),
					LocalDateTime.of(2024, 5, 31, 23, 59, 59),
					"스터디장 설명입니다.", "시간지키세요.", true, true,
					new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY),
					35, 35,
					new MeetingJoinablePart[] {MeetingJoinablePart.PM, MeetingJoinablePart.SERVER},
					1, 2L,
					true, false, false,
					1, "모임개설자", "profile1.jpg",
					List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 32)),
					"010-1234-5678"
				);

			Assertions.assertThat(responseDto.getCreatedTimestamp()).isNotNull();

			Assertions.assertThat(responseDto.getAppliedInfo())
				.extracting(
					"id", "meetingId", "userId", "appliedDate", "status",
					"user.id", "user.name", "user.activities", "user.phone")
				.containsExactly(
					tuple(1, 1, 2,
						LocalDateTime.of(2024, 5, 19, 00, 00, 00, 913489000),
						1,
						2, "승인신청자",
						List.of(new UserActivityVO("기획", 32), new UserActivityVO("기획", 29),
							new UserActivityVO("기획", 33), new UserActivityVO("기획", 30)),
						"010-1111-2222"
					),
					tuple(2, 1, 3,
						LocalDateTime.of(2024, 5, 19, 00, 00, 2, 413489000),
						1,
						3, "승인신청자",
						List.of(new UserActivityVO("웹", 34)),
						"010-3333-4444"
					),
					tuple(3, 1, 4,
						LocalDateTime.of(2024, 5, 19, 00, 00, 3, 413489000),
						0,
						4, "대기신청자",
						List.of(new UserActivityVO("iOS", 32), new UserActivityVO("안드로이드", 29)),
						"010-5555-5555"
					)
				);

		}

		@Test
		@DisplayName("모임과 관련되지 않은 유저가 상세 조회 시, 모임 데이터를 반환한다.")
		void notRelatedMeeting_getMeetingById_success() {
			// given
			Integer meetingId = 1;
			Integer userId = 5;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto)
				.extracting(
					"id", "userId", "title", "category",
					"startDate", "endDate",
					"capacity", "desc", "processDesc",
					"mStartDate", "mEndDate",
					"leaderDesc", "note", "isMentorNeeded", "canJoinOnlyActiveGeneration",
					"createdGeneration", "targetActiveGeneration",
					"joinableParts",
					"status", "approvedApplyCount",
					"host", "apply", "approved",
					"user.id", "user.name", "user.profileImage", "user.activities", "user.phone"
				)
				.containsExactly(
					1, 1, "스터디 구합니다1", "행사",
					LocalDateTime.of(2024, 4, 24, 0, 0, 0),
					LocalDateTime.of(2024, 5, 24, 23, 59, 59),
					10, "스터디 설명입니다.", "스터디 진행방식입니다.",
					LocalDateTime.of(2024, 5, 29, 0, 0, 0),
					LocalDateTime.of(2024, 5, 31, 23, 59, 59),
					"스터디장 설명입니다.", "시간지키세요.", true, true,
					35, 35,
					new MeetingJoinablePart[] {MeetingJoinablePart.PM, MeetingJoinablePart.SERVER},
					1, 2L,
					false, false, false,
					1, "모임개설자", "profile1.jpg",
					List.of(new UserActivityVO("서버", 33), new UserActivityVO("iOS", 32)),
					"010-1234-5678"
				);

			Assertions.assertThat(responseDto.getAppliedInfo())
				.extracting(
					"id", "meetingId", "userId", "appliedDate", "status",
					"user.id", "user.name", "user.activities", "user.phone")
				.containsExactly(
					tuple(1, 1, 2,
						LocalDateTime.of(2024, 5, 19, 00, 00, 00, 913489000),
						1,
						2, "승인신청자",
						List.of(new UserActivityVO("기획", 32), new UserActivityVO("기획", 29),
							new UserActivityVO("기획", 33), new UserActivityVO("기획", 30)),
						"010-1111-2222"
					),
					tuple(2, 1, 3,
						LocalDateTime.of(2024, 5, 19, 00, 00, 2, 413489000),
						1,
						3, "승인신청자",
						List.of(new UserActivityVO("웹", 34)),
						"010-3333-4444"
					),
					tuple(3, 1, 4,
						LocalDateTime.of(2024, 5, 19, 00, 00, 3, 413489000),
						0,
						4, "대기신청자",
						List.of(new UserActivityVO("iOS", 32), new UserActivityVO("안드로이드", 29)),
						"010-5555-5555"
					)
				);

		}

		@Test
		@DisplayName("모임 승인 신청자가 상세 조회 시, apply: true 및 approved: true 를 반환한다.")
		void meetingApprovedApplicant_getMeetingById_success() {
			// given
			Integer meetingId = 1;
			Integer userId = 2;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto)
				.extracting(
					"host", "apply", "approved"
				)
				.containsExactly(
					false, true, true
				);
		}

		@Test
		@DisplayName("모임신청 대기자가 상세 조회 시, apply: true 및 approved: false 를 반환한다.")
		void meetingWaitingApplicant_getMeetingById_success() {
			// given
			Integer meetingId = 1;
			Integer userId = 4;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto)
				.extracting(
					"host", "apply", "approved"
				)
				.containsExactly(
					false, true, false
				);
		}

		@Test
		@DisplayName("명예기수 조회자는 본인이 참여했던 가장 최신 기수 기준 신청중인 멤버 정보를 반환한다.")
		void nonActiveGenerationUser_getMeetingById_participantPartInfo() {
			// given
			Integer meetingId = 1;
			Integer userId = 1;

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.part()).isEqualTo("서버");
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(1);
			Assertions.assertThat(responseDto.isActiveGeneration()).isFalse();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(33);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("승인신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("profile2.jpg");
		}

		@Test
		@DisplayName("활동 기수 서버 조회자는 백엔드 신청자를 같은 파트 참여자로 집계한다.")
		void activeGenerationServerUser_getMeetingById_backendApplicant_samePart() {
			// given
			Integer meetingId = 1;
			User activeGenerationServerUser = userRepository.save(User.builder()
				.name("활동기수서버조회자")
				.orgId(1004)
				.activities(List.of(new UserActivityVO("서버", 35)))
				.profileImage("active-server-profile.jpg")
				.phone("010-1004-1004")
				.build());

			User backendUser = User.builder()
				.name("백엔드신청자")
				.orgId(999)
				.activities(List.of(new UserActivityVO("백엔드", 35)))
				.profileImage("backend-profile.jpg")
				.phone("010-9999-9999")
				.build();
			User savedBackendUser = userRepository.save(backendUser);

			Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
			Apply apply = Apply.builder()
				.type(EnApplyType.APPLY)
				.meeting(meeting)
				.meetingId(meetingId)
				.user(savedBackendUser)
				.userId(savedBackendUser.getId())
				.content("백엔드 신청합니다.")
				.build();
			applyRepository.save(apply);

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				activeGenerationServerUser.getId());

			// then
			Assertions.assertThat(responseDto.part()).isEqualTo("서버");
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(1);
			Assertions.assertThat(responseDto.isActiveGeneration()).isTrue();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(35);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("백엔드신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("backend-profile.jpg");
		}

		@Test
		@DisplayName("여러 명예기수 활동 이력이 있으면 가장 최신 기수 신청자만 반환한다.")
		void honoraryGenerationUser_getMeetingById_latestHonoraryGenerationApplicantInfo() {
			// given
			Integer meetingId = 1;
			User honoraryUser = userRepository.save(User.builder()
				.name("명예기수조회자")
				.orgId(1005)
				.activities(List.of(
					new UserActivityVO("서버", 36),
					new UserActivityVO("디자인", 37)
				))
				.profileImage("honorary-profile.jpg")
				.phone("010-1005-1005")
				.build());

			saveApplyUser(meetingId, 1006, "36기신청자", "기획", 36);
			saveApplyUser(meetingId, 1007, "37기신청자", "웹", 37);
			saveApplyUser(meetingId, 1008, "활동기수신청자", "디자인", 35);

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				honoraryUser.getId());

			// then
			Assertions.assertThat(responseDto.isActiveGeneration()).isFalse();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(37);
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(1);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("37기신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("profile.jpg");
		}

		@Test
		@DisplayName("활동 이력이 없는 사용자도 상세 조회 시 모임 데이터를 반환한다.")
		void noActivityUser_getMeetingById_success() {
			Integer meetingId = 1;
			User noActivityUser = userRepository.save(User.builder()
				.name("활동없음조회자")
				.orgId(2001)
				.activities(null)
				.profileImage("no-activity-profile.jpg")
				.phone("010-2001-2001")
				.build());

			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				noActivityUser.getId());

			Assertions.assertThat(responseDto.getId()).isEqualTo(meetingId);
		}

		@Test
		@DisplayName("모임 신청기간 전인 경우, status 0 를 반환한다.")
		void beforeApply_getMeetingById_success() {
			// given
			Integer meetingId = 2;
			Integer userId = 4;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.getStatus()).isEqualTo(0);
		}

		@Test
		@DisplayName("모임 신청기간 중인 경우, status 1 를 반환한다.")
		void applying_getMeetingById_success() {
			// given
			Integer meetingId = 1;
			Integer userId = 4;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.getStatus()).isEqualTo(1);
		}

		@Test
		@DisplayName("모임 신청기간 종료 후의 경우, status 2 를 반환한다.")
		void afterApply_getMeetingById_success() {
			// given
			Integer meetingId = 3;
			Integer userId = 4;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.getStatus()).isEqualTo(2);
		}

		@Test
		@DisplayName("모임 조회 시, 승인된 신청자 수를 반환한다.")
		void getMeeting_getMeetingById_approvedApplyCount() {
			// given
			Integer meetingId = 1;
			Integer userId = 4;

			// when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.getApprovedApplyCount()).isEqualTo(2);
		}

		@Test
		@DisplayName("모임 신청자 인원 조회시, 정렬된 정보여야 하고 신청 번호 조회시 연속적인 번호가 나와야한다.")
		void getMeeting_extract_field() {
			//given
			Integer meetingId = 1;
			Integer userId = 4;

			//when
			MeetingV2GetMeetingByIdResponseDto responseDto = meetingV2Service.getMeetingDetail(meetingId,
				userId);

			//then
			int size = responseDto.getAppliedInfo().size(); // 사이즈 값 까지의 값이 하나씩 있어야 한다.
			List<Integer> applyNumbers = responseDto.getAppliedInfo().stream()
				.map(ApplyWholeInfoDto::getApplyNumber)
				.toList();
			List<Integer> expectedNumbers = IntStream.rangeClosed(1, size)
				.boxed().toList();

			Assertions.assertThat(responseDto.getAppliedInfo())
				.isSortedAccordingTo(Comparator.comparing(ApplyWholeInfoDto::getAppliedDate));
			Assertions.assertThat(applyNumbers).isEqualTo(expectedNumbers);
		}

	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 모임_같은_파트_참여_멤버_조회 {

		@Test
		@DisplayName("활동 기수 조회자는 현재 활동 기수 파트 기준 참여중인 멤버 리스트를 반환한다.")
		void activeGenerationUser_getMeetingPartMembers_success() {
			// given
			Integer meetingId = 1;
			Integer userId = 5;
			saveApplyUser(meetingId, 1009, "35기iOS신청자", "iOS", 35);

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.part()).isEqualTo("iOS");
			Assertions.assertThat(responseDto.isActiveGeneration()).isTrue();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(35);
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(1);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("35기iOS신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("profile.jpg");
		}

		@Test
		@DisplayName("명예기수 조회자는 마지막 활동 기수 신청자를 멤버로 조회한다.")
		void serverUser_getMeetingPartMembers_backendApplicant_samePart() {
			// given
			Integer meetingId = 1;
			Integer userId = 1;

			saveApplyUser(meetingId, 1000, "33기백엔드신청자", "백엔드", 33);

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.part()).isEqualTo("서버");
			Assertions.assertThat(responseDto.isActiveGeneration()).isFalse();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(33);
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(2);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1, 2);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("승인신청자", "33기백엔드신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("profile2.jpg", "profile.jpg");
		}

		@Test
		@DisplayName("기획 조회자는 PM 신청자를 같은 파트 멤버로 조회한다.")
		void planUser_getMeetingPartMembers_pmApplicant_samePart() {
			// given
			Integer meetingId = 1;
			Integer userId = 2;

			saveApplyUser(meetingId, 1001, "PM신청자", "PM", 33);

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				userId);

			// then
			Assertions.assertThat(responseDto.part()).isEqualTo("기획");
			Assertions.assertThat(responseDto.isActiveGeneration()).isFalse();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(33);
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(2);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1, 2);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("승인신청자", "PM신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("profile2.jpg", "profile.jpg");
		}

		@Test
		@DisplayName("웹 조회자는 프론트엔드 신청자를 같은 파트 멤버로 조회한다.")
		void webUser_getMeetingPartMembers_frontendApplicant_samePart() {
			// given
			Integer meetingId = 4;
			User webUser = userRepository.save(User.builder()
				.name("웹조회자")
				.orgId(1002)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("web-profile.jpg")
				.phone("010-0000-0000")
				.build());

			saveApplyUser(meetingId, 1003, "프론트엔드신청자", "프론트엔드", 35);

			// when
			MeetingV2GetMeetingPartMembersResponseDto responseDto = meetingV2Service.getMeetingPartMembers(meetingId,
				webUser.getId());

			// then
			Assertions.assertThat(responseDto.part()).isEqualTo("웹");
			Assertions.assertThat(responseDto.isActiveGeneration()).isTrue();
			Assertions.assertThat(responseDto.activeGeneration()).isEqualTo(35);
			Assertions.assertThat(responseDto.participantCount()).isEqualTo(1);
			Assertions.assertThat(responseDto.memberIds()).containsExactly(1);
			Assertions.assertThat(responseDto.memberNames()).containsExactly("프론트엔드신청자");
			Assertions.assertThat(responseDto.memberProfileImages()).containsExactly("profile.jpg");
		}

		@Test
		@DisplayName("활동 이력이 없는 사용자가 모집현황 멤버 조회 시 예외가 발생한다.")
		void noActivityUser_getMeetingPartMembers_throwException() {
			Integer meetingId = 1;
			User noActivityUser = userRepository.save(User.builder()
				.name("활동없음조회자")
				.orgId(2002)
				.activities(null)
				.profileImage("no-activity-profile.jpg")
				.phone("010-2002-2002")
				.build());

			Assertions.assertThatThrownBy(() -> meetingV2Service.getMeetingPartMembers(meetingId, noActivityUser.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining(MISSING_GENERATION_PART.getErrorCode());
		}

	}

	@Nested
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 지원자_참여자_조회 {
		@Test
		@DisplayName("지원자/참여자를 조회할 경우, 지원자 목록 10개를 반환한다.")
		void returns_10_applicants_when_querying_for_applicants_participants() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
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
					.activities(List.of(new UserActivityVO("기획", 34)))
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

			for (int i = 13; i <= 14; i++) {
				User applicant3 = User.builder()
					.name("지원자 " + i)
					.orgId(i + 1)
					.activities(List.of(new UserActivityVO("디자인", 32)))
					.profileImage("applicantProfile" + i + ".jpg")
					.phone("010-1234-56" + (78 + i))
					.build();

				userRepository.save(applicant3);

				Apply apply3 = Apply.builder()
					.type(EnApplyType.APPLY)
					.meeting(meeting)
					.meetingId(meeting.getId())
					.user(applicant3)
					.userId(100 + i)
					.content("지원 동기 " + i)
					.build();

				apply3.updateApplyStatus(EnApplyStatus.REJECT);

				applyRepository.save(apply3);
			}

			Integer meetingId = meeting.getId();
			Integer userId = leader.getId();

			MeetingGetAppliesQueryDto queryCommand = MeetingGetAppliesQueryDto.builder()
				.page(1)
				.take(10)
				.status(List.of(EnApplyStatus.APPROVE, EnApplyStatus.WAITING))
				.date("desc")
				.build();

			// when
			MeetingGetApplyListResponseDto responseDto = meetingV2Service.findApplyList(queryCommand, meetingId,
				userId);
			PageRequest pageable = PageRequest.of(queryCommand.getPage() - 1, queryCommand.getTake());
			Page<ApplyInfoDto> applyList = applyRepository.findApplyList(queryCommand, pageable, meetingId,
				meeting.getUserId(), userId);

			// then
			Assertions.assertThat(responseDto).isNotNull(); // responseDto가 null이 아님을 확인
			Assertions.assertThat(responseDto.getApply()).isNotEmpty();  // 신청 목록이 비어 있지 않음을 확인
			Assertions.assertThat(responseDto.getMeta().getPage()).isEqualTo(1);  // 페이지 정보가 예상대로인지 검증
			Assertions.assertThat(responseDto.getMeta().getTake()).isEqualTo(10);  // 한 페이지당 지원자 수가 10명인지 검증

			// 총 지원자 수 비교
			Assertions.assertThat(applyList.getTotalElements()).isEqualTo(responseDto.getMeta().getItemCount());

			// 지원자 목록 필드별 검증
			Assertions.assertThat(responseDto.getApply())
				.extracting("user.name", "user.orgId", "user.recentActivity.part", "user.recentActivity.generation",
					"user.profileImage", "user.phone", "content", "status")
				.containsExactly(
					tuple("지원자 12", 13, "기획", 34, "applicantProfile12.jpg", "010-1234-5690", "지원 동기 12",
						EnApplyStatus.WAITING),
					tuple("지원자 11", 12, "기획", 34, "applicantProfile11.jpg", "010-1234-5689", "지원 동기 11",
						EnApplyStatus.WAITING),
					tuple("지원자 10", 11, "기획", 34, "applicantProfile10.jpg", "010-1234-5688", "지원 동기 10",
						EnApplyStatus.WAITING),
					tuple("지원자 9", 10, "기획", 34, "applicantProfile9.jpg", "010-1234-5687", "지원 동기 9",
						EnApplyStatus.WAITING),
					tuple("지원자 8", 9, "기획", 34, "applicantProfile8.jpg", "010-1234-5686", "지원 동기 8",
						EnApplyStatus.WAITING),
					tuple("지원자 7", 8, "기획", 34, "applicantProfile7.jpg", "010-1234-5685", "지원 동기 7",
						EnApplyStatus.WAITING),
					tuple("지원자 6", 7, "안드로이드", 35, "applicantProfile6.jpg", "010-1234-5684", "지원 동기 6",
						EnApplyStatus.APPROVE),
					tuple("지원자 5", 6, "안드로이드", 35, "applicantProfile5.jpg", "010-1234-5683", "지원 동기 5",
						EnApplyStatus.APPROVE),
					tuple("지원자 4", 5, "안드로이드", 35, "applicantProfile4.jpg", "010-1234-5682", "지원 동기 4",
						EnApplyStatus.APPROVE),
					tuple("지원자 3", 4, "안드로이드", 35, "applicantProfile3.jpg", "010-1234-5681", "지원 동기 3",
						EnApplyStatus.APPROVE));

			// ApplyInfoDto의 모든 필드 비교
			Assertions.assertThat(applyList.getContent()).usingRecursiveComparison()  // 객체의 필드를 재귀적으로 비교
				.isEqualTo(responseDto.getApply());
		}
	}

	@Nested
	class 모임_지원_취소 {
		@Test
		@DisplayName("모임 지원 취소를 할 경우, 해당 지원자가 지원 내역에서 삭제되어야 한다.")
		void applyMeetingCancel_Success() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 취소 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 10, 6, 0, 0, 0))
				.endDate(LocalDateTime.of(2024, 10, 7, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 취소 테스트입니다.")
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

			User applicant1 = User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("iOS", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(applicant1);

			Apply apply1 = Apply.builder()
				.type(EnApplyType.APPLY)
				.meeting(meeting)
				.meetingId(meeting.getId())
				.user(applicant1)
				.userId(applicant1.getId())
				.content("지원 동기")
				.build();

			applyRepository.save(apply1);

			Integer meetingId = meeting.getId();
			Integer userId = applicant1.getId();

			// when
			meetingV2Service.applyMeetingCancel(meetingId, userId);

			// then
			boolean exists = applyRepository.existsByMeetingIdAndUserId(meetingId, userId);
			Assertions.assertThat(exists).isFalse();
		}

		@Test
		@DisplayName("지원하지 않은 사용자가 모임을 취소하려고 할 경우, 예외가 발생해야 한다.")
		void applyMeetingCancel_NotAppliedUser_Failure() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 취소 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 10, 6, 0, 0, 0))
				.endDate(LocalDateTime.of(2024, 10, 7, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 취소 테스트입니다.")
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

			Integer meetingId = meeting.getId();
			Integer nonExistentUserId = 999; // 존재하지 않는 userId

			// when & then
			Assertions.assertThatThrownBy(() -> meetingV2Service.applyMeetingCancel(meetingId, nonExistentUserId))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("신청상태가 아닌 모임입니다.");
		}
	}

	@Nested
	class 모임_지원 {
		@Test
		@DisplayName("정상적인 모임 지원 시, 지원 내역이 성공적으로 저장된다.")
		void applyMeeting_ShouldSaveSuccessfully() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
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

			User applicant = User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(applicant);

			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");

			// when
			MeetingV2ApplyMeetingResponseDto response = meetingV2Service.applyGeneralMeeting(applyDto,
				applicant.getId());

			// then
			Apply apply = applyRepository.findById(response.getApplyId()).orElseThrow();
			Assertions.assertThat(apply.getUser().getId()).isEqualTo(applicant.getId());
		}

		@Test
		@DisplayName("모임 승인 정원이 초과된 경우, 지원할 수 없다.")
		void applyMeeting_ShouldFailWhenCapacityExceeded() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 10, 7, 23, 59, 59))
				.capacity(1)
				.desc("모임 지원 테스트입니다.")
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

			User applicant1 = User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("applicantProfile1.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(applicant1);

			// Create and save the apply entity
			MeetingV2ApplyMeetingDto applyDto1 = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기 1");
			Apply apply1 = applyMapper.toApplyEntity(applyDto1, EnApplyType.APPLY, meeting, applicant1,
				applicant1.getId());
			apply1.updateApplyStatus(EnApplyStatus.APPROVE);
			applyRepository.save(apply1);

			User applicant2 = User.builder()
				.name("지원자 2")
				.orgId(3)
				.activities(List.of(new UserActivityVO("서버", 34)))
				.profileImage("applicantProfile2.jpg")
				.phone("010-1234-5679")
				.build();
			userRepository.save(applicant2);

			MeetingV2ApplyMeetingDto applyDto2 = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기 2");

			// when & then
			Assertions.assertThatThrownBy(() -> meetingV2Service.applyGeneralMeeting(applyDto2, applicant2.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("정원이 꽉 찼습니다.");
		}

		@Test
		@DisplayName("이미 지원한 사용자는 다시 지원할 수 없다.")
		void applyMeeting_Fail_WhenAlreadyApplied() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("iOS", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 10, 7, 23, 59, 59))
				.capacity(50)
				.desc("모임 지원 테스트입니다.")
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

			User applicant = userRepository.save(User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("디자인", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build());

			// 모임에 이미 지원한 상태
			Apply apply = Apply.builder()
				.type(EnApplyType.APPLY)
				.meeting(meeting)
				.meetingId(meeting.getId())
				.user(applicant)
				.userId(applicant.getId())
				.content("지원 동기 ")
				.build();

			applyRepository.save(apply);

			// when & then
			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");
			Assertions.assertThatThrownBy(() -> meetingV2Service.applyGeneralMeeting(applyDto, applicant.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("이미 지원한 모임입니다.");
		}

		@Test
		@DisplayName("지원 기간 외에는 지원할 수 없다.")
		void applyMeeting_Fail_WhenOutsideApplicationPeriod() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("iOS", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2030, 4, 7, 0, 0, 0))
				.endDate(LocalDateTime.of(2030, 10, 7, 23, 59, 59))
				.capacity(50)
				.desc("모임 지원 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2025, 11, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2025, 12, 24, 0, 0, 0))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build();

			meetingRepository.save(meeting);

			User applicant = userRepository.save(User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("디자인", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build());

			// when & then
			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");
			Assertions.assertThatThrownBy(() -> meetingV2Service.applyGeneralMeeting(applyDto, applicant.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessageContaining("지원 기간이 아닙니다.");
		}

		@Test
		@DisplayName("공동 모임장은 지원할 수 없다.")
		void applyMeeting_Fail_WhenIsCoLeader() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("iOS", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			User coLeaderUser1 = User.builder()
				.name("공동 모임장1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("서버", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-2222-2222")
				.build();

			User coLeaderUser2 = User.builder()
				.name("공동 모임장2")
				.orgId(3)
				.activities(List.of(new UserActivityVO("기획", 33)))
				.profileImage("testProfileImage.jpg")
				.phone("010-3333-3333")
				.build();
			User savedLeader = userRepository.save(leader);
			User savedCoLeader1 = userRepository.save(coLeaderUser1);
			User savedCoLeader2 = userRepository.save(coLeaderUser2);

			Meeting meeting = Meeting.builder()
				.user(savedLeader)
				.userId(savedLeader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2024, 5, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 5, 30, 23, 59, 59))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build();

			meetingRepository.save(meeting);

			CoLeader coLeader1 = CoLeader.builder()
				.meeting(meeting)
				.user(savedCoLeader1)
				.build();

			CoLeader coLeader2 = CoLeader.builder()
				.meeting(meeting)
				.user(savedCoLeader2)
				.build();
			coLeaderRepository.saveAll(List.of(coLeader1, coLeader2));

			// when & then
			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");
			Assertions.assertThatThrownBy(() -> meetingV2Service.applyGeneralMeeting(applyDto, savedCoLeader1.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("공동 모임장은 신청할 수 없습니다.");
		}

		@Test
		@DisplayName("모임장은 지원할 수 없다.")
		void applyMeeting_Fail_WhenIsLeader() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("iOS", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.STUDY)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
				.processDesc("테스트 진행 방식입니다.")
				.mStartDate(LocalDateTime.of(2024, 5, 24, 0, 0, 0))
				.mEndDate(LocalDateTime.of(2024, 5, 30, 23, 59, 59))
				.leaderDesc("모임 리더 설명입니다.")
				.note("유의사항입니다.")
				.isMentorNeeded(false)
				.canJoinOnlyActiveGeneration(false)
				.createdGeneration(35)
				.targetActiveGeneration(null)
				.joinableParts(MeetingJoinablePart.values())
				.build();

			meetingRepository.save(meeting);

			// when & then
			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");
			Assertions.assertThatThrownBy(() -> meetingV2Service.applyGeneralMeeting(applyDto, leader.getId()))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("모임장은 신청할 수 없습니다.");
		}

		@Test
		@DisplayName("행사 카테고리에 일반 모임 신청 API로 호출 시 예외가 발생되어야 한다.")
		void applyGeneralMeeting_WhenEventCategory_ShouldThrowException() {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(MeetingCategory.EVENT)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
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

			User applicant = User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(applicant);

			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");

			// when & then
			Assertions.assertThatThrownBy(() ->
					meetingV2Service.applyGeneralMeeting(applyDto, applicant.getId())
				).isInstanceOf(BadRequestException.class)
				.hasMessageContaining(INVALID_MEETING_CATEGORY.getErrorCode());
		}

		@ParameterizedTest
		@EnumSource(value = MeetingCategory.class, names = "EVENT", mode = EnumSource.Mode.EXCLUDE)
		@DisplayName("행사 카테고리가 아닌 다른 카테고리로 행사 모임 신청 API로 호출 시 예외가 발생되어야 한다.")
		void applyEventMeeting_WhenCategoryIsNotEvent_ShouldThrowException(MeetingCategory category) {
			// given
			User leader = User.builder()
				.name("모임장")
				.orgId(1)
				.activities(List.of(new UserActivityVO("안드로이드", 35)))
				.profileImage("testProfileImage.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(leader);

			Meeting meeting = Meeting.builder()
				.user(leader)
				.userId(leader.getId())
				.title("모임 지원 테스트")
				.category(category)
				.imageURL(List.of(new ImageUrlVO(0, "testImage.jpg")))
				.startDate(LocalDateTime.of(2024, 4, 23, 0, 0, 0))
				.endDate(LocalDateTime.of(2029, 4, 27, 23, 59, 59))
				.capacity(20)
				.desc("모임 지원 테스트입니다.")
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

			User applicant = User.builder()
				.name("지원자 1")
				.orgId(2)
				.activities(List.of(new UserActivityVO("웹", 35)))
				.profileImage("applicantProfile.jpg")
				.phone("010-1234-5678")
				.build();

			userRepository.save(applicant);

			MeetingV2ApplyMeetingDto applyDto = new MeetingV2ApplyMeetingDto(meeting.getId(), "지원 동기");

			// when & then
			Assertions.assertThatThrownBy(() ->
					meetingV2Service.applyEventMeeting(applyDto, applicant.getId())
				).isInstanceOf(BadRequestException.class)
				.hasMessageContaining(INVALID_MEETING_CATEGORY.getErrorCode());
		}

	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 모임_지원자_상태_변경 {
		@Test
		@DisplayName("공동 모임장은 모임 지원자 상태 변경을 할 수 없다.")
		void updateApplyStatus_Fail_isCoLeader() {
			// given
			Integer coLeaderId = 5;
			ApplyV2UpdateStatusBodyDto dto = new ApplyV2UpdateStatusBodyDto(1, 1);

			// when, then
			Assertions.assertThatThrownBy(
					() -> meetingV2Service.updateApplyStatus(1, dto, coLeaderId))
				.isInstanceOf(ForbiddenException.class)
				.hasMessage(FORBIDDEN_EXCEPTION.getErrorCode());
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 모임_수정 {
		@Test
		@DisplayName("공동 모임장은 모임을 수정할 수 없다.")
		void modifyMeeting_Fail_isCoLeader() {
			// given
			Integer coLeaderId = 5;

			MeetingV2UpdateMeetingBodyDto dto = new MeetingV2UpdateMeetingBodyDto(
				null, // title
				null, // subTitle
				null, // files
				null, // category
				null, // startDate
				null, // endDate
				null, // capacity
				null, // desc
				null, // processDesc
				null, // mStartDate
				null, // mEndDate
				null, // leaderDesc
				null, // note
				null, // isMentorNeeded
				null, // canJoinOnlyActiveGeneration
				null, // joinInfo
				null, // joinableParts
				null, // coLeaderUserIds
				List.of("초면 환영") // welcomeMessageTypes
			);
			// when, then
			Assertions.assertThatThrownBy(
					() -> meetingV2Service.updateMeeting(1, dto, coLeaderId))
				.isInstanceOf(ForbiddenException.class)
				.hasMessage(FORBIDDEN_EXCEPTION.getErrorCode());
		}

		@Test
		@DisplayName("새로운 모임 수정 페이지는 부제목과 참여 정보만 수정할 수 있다.")
		void patchMeeting_newPage_success() {
			// given
			Integer userId = 1;
			Integer meetingId = 1;

			MeetingJoinInfo joinInfo = new MeetingJoinInfo(MeetingType.OFFLINE, MeetingFrequency.IMMERSIVE);

			MeetingV2UpdateMeetingBodyDto dto = new MeetingV2UpdateMeetingBodyDto(
				null, // title
				"수정 부제목", // subTitle
				null, // files
				null, // category
				null, // startDate
				null, // endDate
				null, // capacity
				null, // desc
				null, // processDesc
				null, // mStartDate
				null, // mEndDate
				null, // leaderDesc
				null, // note
				null, // isMentorNeeded
				null, // canJoinOnlyActiveGeneration
				joinInfo, // joinInfo (참여 정보)
				null, // joinableParts
				null, // coLeaderUserIds
				null // welcomeMessageTypes
			);

			// when
			meetingV2Service.updateMeeting(meetingId, dto, userId);

			// then
			Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
			Assertions.assertThat(meeting)
				.extracting("title", "subTitle", "joinInfo", "category")
				.containsExactly("스터디 구합니다1", "수정 부제목", joinInfo, MeetingCategory.EVENT);

			List<CoLeader> coLeaders = coLeaderRepository.findAllByMeetingId(meetingId);
			Assertions.assertThat(coLeaders).hasSize(1);
			Assertions.assertThat(coLeaders)
				.extracting("user.name")
				.containsExactly("모임개설자2");
		}

		@Test
		@DisplayName("기존 개설자 수정 페이지는 환영 태그만 수정할 수 있다.")
		void patchMeeting_legacyPage_success() {
			// given
			Integer userId = 1;
			Integer meetingId = 1;

			MeetingV2UpdateMeetingBodyDto dto = new MeetingV2UpdateMeetingBodyDto(
				null, // title
				null, // subTitle
				null, // files
				null, // category
				null, // startDate
				null, // endDate
				null, // capacity
				null, // desc
				null, // processDesc
				null, // mStartDate
				null, // mEndDate
				null, // leaderDesc
				null, // note
				null, // isMentorNeeded
				null, // canJoinOnlyActiveGeneration
				null, // joinInfo
				null, // joinableParts
				null, // coLeaderUserIds
				List.of("초면 환영") // welcomeMessageTypes
			);

			// when
			meetingV2Service.updateMeeting(meetingId, dto, userId);

			// then
			Assertions.assertThat(tagV2Service.getWelcomeMessageTypesByMeetingId(meetingId))
				.extracting("value")
				.containsExactly("초면 환영");

			Meeting meeting = meetingRepository.findByIdOrThrow(meetingId);
			Assertions.assertThat(meeting.getSubTitle()).isEqualTo("스터디 부제목1");
			Assertions.assertThat(meeting.getJoinInfo()).isEqualTo(
				new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY));
		}
	}

	@Nested
	@SqlGroup({
		@Sql(value = "/sql/meeting-service-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
		@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)

	})
	class 모임_삭제 {
		@Test
		@DisplayName("공동 모임장은 모임을 삭제할 수 없다.")
		void deleteMeeting_Fail_isCoLeader() {
			// given
			Integer coLeaderId = 5;

			// when, then
			Assertions.assertThatThrownBy(
					() -> meetingV2Service.deleteMeeting(1, coLeaderId))
				.isInstanceOf(ForbiddenException.class)
				.hasMessage(FORBIDDEN_EXCEPTION.getErrorCode());
		}
	}
}
