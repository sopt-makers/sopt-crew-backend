package org.sopt.makers.crew.main.meeting.v2.repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.MeetingRepository;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.meeting.vo.ImageUrlVO;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.springframework.beans.factory.annotation.Autowired;

@IntegratedTest
public class MeetingRepositoryTest {

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("정상적인 경우, 모임 저장시 성공한다.")
	void normal_save_success() {
		// given
		User user = User.builder()
			.name("홍길동")
			.orgId(1)
			.activities(null)
			.profileImage("image-url1")
			.phone("010-1234-5678")
			.build();
		User savedUser = userRepository.save(user);
		// 어 이거 왜 동등성 보장이 안되는거야?

		List<ImageUrlVO> imageUrlList = Arrays.asList(
			new ImageUrlVO(1, "https://example.com/image1.jpg"),
			new ImageUrlVO(2, "https://example.com/image2.jpg")
		);

		// Meeting 객체 생성
		Meeting meeting = Meeting.builder()
			.user(savedUser)
			.userId(1)  // userId 예시
			.title("Backend 개발 스터디")
			.category(MeetingCategory.STUDY)
			.imageURL(imageUrlList)
			.startDate(LocalDateTime.of(2024, 10, 1, 9, 0))
			.endDate(LocalDateTime.of(2024, 10, 15, 18, 0))
			.capacity(10)
			.desc("백엔드 개발에 관심 있는 사람들을 위한 스터디입니다.")
			.processDesc("매주 온라인으로 진행되며, 발표와 토론이 포함됩니다.")
			.mStartDate(LocalDateTime.of(2024, 10, 16, 9, 0))
			.mEndDate(LocalDateTime.of(2024, 12, 1, 18, 0))
			.leaderDesc("5년차 백엔드 개발자입니다.")
			.note("준비물은 노트북과 열정입니다.")
			.isMentorNeeded(false)
			.canJoinOnlyActiveGeneration(true)
			.createdGeneration(2024)
			.targetActiveGeneration(2024)
			.joinableParts(new MeetingJoinablePart[] {MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS})
			.build();

		// when
		Meeting savedMeeting = meetingRepository.save(meeting);

		// then
		Assertions.assertThat(savedMeeting)
			.isNotNull()
			.extracting(
				"user", "userId", "title", "category", "imageURL", "startDate", "endDate", "capacity", "desc",
				"processDesc", "mStartDate", "mEndDate", "leaderDesc", "note", "isMentorNeeded",
				"canJoinOnlyActiveGeneration", "createdGeneration", "targetActiveGeneration", "joinableParts"
			)
			.containsExactly(
				savedUser,  // user 필드
				savedUser.getId(),  // userId 필드
				"Backend 개발 스터디",  // title 필드
				MeetingCategory.STUDY,  // category 필드
				imageUrlList,  // imageURL 필드
				LocalDateTime.of(2024, 10, 1, 9, 0),  // startDate 필드
				LocalDateTime.of(2024, 10, 15, 18, 0),  // endDate 필드
				10,  // capacity 필드
				"백엔드 개발에 관심 있는 사람들을 위한 스터디입니다.",  // desc 필드
				"매주 온라인으로 진행되며, 발표와 토론이 포함됩니다.",  // processDesc 필드
				LocalDateTime.of(2024, 10, 16, 9, 0),  // mStartDate 필드
				LocalDateTime.of(2024, 12, 1, 18, 0),  // mEndDate 필드
				"5년차 백엔드 개발자입니다.",  // leaderDesc 필드
				"준비물은 노트북과 열정입니다.",  // note 필드
				false,  // isMentorNeeded 필드
				true,  // canJoinOnlyActiveGeneration 필드
				2024,  // createdGeneration 필드
				2024,  // targetActiveGeneration 필드
				new MeetingJoinablePart[] {MeetingJoinablePart.SERVER, MeetingJoinablePart.IOS}  // joinableParts 필드
			);

		// 추가적으로 imageURL 리스트도 개별적으로 검증
		Assertions.assertThat(savedMeeting.getImageURL())
			.hasSize(2)
			.extracting("url")
			.containsExactly(
				"https://example.com/image1.jpg",
				"https://example.com/image2.jpg"
			);
	}
}
