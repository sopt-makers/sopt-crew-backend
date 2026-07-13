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
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserRepository;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;

@IntegratedTest
public class MeetingRepositoryTest {

	@Autowired
	private MeetingRepository meetingRepository;

	@Autowired
	private MeetingDemandRepository meetingDemandRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private EntityManager entityManager;

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
				"userId", "title", "category", "imageURL", "startDate", "endDate", "capacity", "desc",
				"processDesc", "mStartDate", "mEndDate", "leaderDesc", "note", "isMentorNeeded",
				"canJoinOnlyActiveGeneration", "createdGeneration", "targetActiveGeneration", "joinableParts"
			)
			.containsExactly(
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

	@Test
	@DisplayName("모임 수요 기반으로 개설된 모임의 수요 연결을 해제한다.")
	void clearMeetingDemandId_success() {
		User user = userRepository.save(User.builder()
			.name("홍길동")
			.orgId(1)
			.activities(null)
			.profileImage("image-url1")
			.phone("010-1234-5678")
			.build());
		MeetingDemand meetingDemand = meetingDemandRepository.save(MeetingDemand.builder()
			.user(user)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of(MeetingKeywordType.EXERCISE))
			.build());
		Meeting meeting = meetingRepository.save(Meeting.builder()
			.user(user)
			.meetingDemandId(meetingDemand.getId())
			.title("러닝 모임")
			.category(MeetingCategory.STUDY)
			.imageURL(List.of(new ImageUrlVO(1, "https://example.com/image1.jpg")))
			.startDate(LocalDateTime.of(2026, 7, 1, 9, 0))
			.endDate(LocalDateTime.of(2026, 7, 15, 18, 0))
			.capacity(10)
			.desc("러닝을 함께 합니다.")
			.processDesc("매주 오프라인으로 진행합니다.")
			.mStartDate(LocalDateTime.of(2026, 7, 16, 9, 0))
			.mEndDate(LocalDateTime.of(2026, 8, 1, 18, 0))
			.isMentorNeeded(false)
			.canJoinOnlyActiveGeneration(true)
			.createdGeneration(36)
			.targetActiveGeneration(36)
			.joinableParts(new MeetingJoinablePart[] {MeetingJoinablePart.SERVER})
			.build());

		meetingRepository.clearMeetingDemandId(meetingDemand.getId());
		entityManager.clear();

		Assertions.assertThat(meetingRepository.findByIdOrThrow(meeting.getId()).getMeetingDemandId()).isNull();
	}
}
