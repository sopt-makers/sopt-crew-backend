package org.sopt.makers.crew.main.meeting.v2.service;

import static org.sopt.makers.crew.main.common.constant.CrewConst.*;
import static org.sopt.makers.crew.main.common.exception.ErrorStatus.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sopt.makers.crew.main.common.annotation.IntegratedTest;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;
import org.sopt.makers.crew.main.meeting.v2.dto.request.MeetingV2CreateMeetingBodyDto;
import org.sopt.makers.crew.main.meeting.v2.dto.response.MeetingV2CreateMeetingResponseDto;
import org.springframework.beans.factory.annotation.Autowired;

@IntegratedTest
public class MeetingV2ServiceTest {

	@Autowired
	private MeetingV2Service meetingV2Service;

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private UserRepository userRepository;

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

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
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
			MeetingV2CreateMeetingResponseDto responseDto = meetingV2Service.createMeeting(meetingDto, savedUser.getId());
			Meeting foundMeeting = meetingRepository.findByIdOrThrow(responseDto.getMeetingId());

			// then
			Assertions.assertThat(foundMeeting.getId()).isEqualTo(responseDto.getMeetingId());

			Assertions.assertThat(foundMeeting)
				.isNotNull()
				.extracting(
					"user", "userId", "title", "category", "startDate", "endDate", "capacity", "desc",
					"processDesc", "mStartDate", "mEndDate", "leaderDesc", "note", "isMentorNeeded",
					"canJoinOnlyActiveGeneration", "createdGeneration", "targetActiveGeneration", "joinableParts"
				)
				.containsExactly(
					savedUser,  // user 필드
					savedUser.getId(),  // userId 필드
					"알고보면 쓸데있는 개발 프로세스",  // title 필드
					MeetingCategory.STUDY,  // category 필드
					LocalDateTime.of(2024, 10, 1, 0, 0,0),  // startDate 필드
					LocalDateTime.of(2024, 10, 15, 23, 59,59),  // endDate 필드
					10,  // capacity 필드
					"백엔드 개발에 관심 있는 사람들을 위한 스터디입니다.",  // desc 필드
					"매주 온라인으로 진행되며, 발표와 토론이 포함됩니다.",  // processDesc 필드
					LocalDateTime.of(2024, 10, 16, 0, 0,0),  // mStartDate 필드
					LocalDateTime.of(2024, 12, 30, 23, 59, 59),  // mEndDate 필드
					"5년차 백엔드 개발자입니다.",  // leaderDesc 필드
					"준비물은 노트북과 열정입니다.",  // note 필드
					false,  // isMentorNeeded 필드
					canJoinOnlyActiveGeneration,  // canJoinOnlyActiveGeneration 필드
					ACTIVE_GENERATION,  // createdGeneration 필드
					canJoinOnlyActiveGeneration ? ACTIVE_GENERATION : null,  // targetActiveGeneration 필드
					new MeetingJoinablePart[]{MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS}  // joinableParts 필드
				);

			Assertions.assertThat(foundMeeting.getImageURL())
				.hasSize(1)
				.extracting("url")
				.containsExactly(
					"https://example.com/image1.jpg"
				);
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

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
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
				"https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2023/04/12/7bd87736-b557-4b26-a0d5-9b09f1f1d7df"
			);

			// 대상 파트 목록
			MeetingJoinablePart[] joinableParts = null;

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
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
			MeetingJoinablePart[] joinableParts = {
				MeetingJoinablePart.ANDROID,
				MeetingJoinablePart.IOS
			};

			// DTO 생성
			MeetingV2CreateMeetingBodyDto meetingDto = new MeetingV2CreateMeetingBodyDto(
				"알고보면 쓸데있는 개발 프로세스", // title
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
}
