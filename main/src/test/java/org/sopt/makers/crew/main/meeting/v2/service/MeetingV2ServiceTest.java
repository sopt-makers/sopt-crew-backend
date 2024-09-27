package org.sopt.makers.crew.main.meeting.v2.service;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.common.annotation.IntegratedTest;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
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

	@Test
	@DisplayName("모임 생성 성공 시, 생성된 모임 번호가 반환된다.")
	void normal_createMeeting_meetingId(){
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

		// when
		MeetingV2CreateMeetingResponseDto meeting = meetingV2Service.createMeeting(meetingDto, savedUser.getId());
		Meeting foundMeeting = meetingRepository.findByIdOrThrow(meeting.getMeetingId());

		// then
		Assertions.assertThat(meeting.getMeetingId()).isEqualTo(foundMeeting.getId());
	}
}
