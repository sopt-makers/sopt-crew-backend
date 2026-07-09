package org.sopt.makers.crew.main.meetingdemandcomment.v2.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingFrequency;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingType;
import org.sopt.makers.crew.main.entity.meeting.vo.MeetingJoinInfo;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemand;
import org.sopt.makers.crew.main.entity.meetingdemand.MeetingDemandRepository;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfile;
import org.sopt.makers.crew.main.entity.meetingdemandcomment.MeetingDemandCommentProfileRepository;
import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.UserFixture;

@ExtendWith(MockitoExtension.class)
class MeetingDemandCommentProfileFactoryTest {

	private static final int MEETING_DEMAND_ID = 10;
	private static final int WRITER_ID = 1;
	private static final String MEETING_DEMAND_ANONYMOUS_NICKNAME = "성실한 토마토";
	private static final int MEETING_DEMAND_ANONYMOUS_IMAGE_NUMBER = 4;

	@Mock
	private MeetingDemandCommentProfileRepository meetingDemandCommentProfileRepository;

	@Mock
	private MeetingDemandRepository meetingDemandRepository;

	private MeetingDemandCommentProfileFactory meetingDemandCommentProfileFactory;

	private MeetingDemand meetingDemand;

	@BeforeEach
	void setUp() {
		meetingDemandCommentProfileFactory = new MeetingDemandCommentProfileFactory(
			meetingDemandCommentProfileRepository,
			meetingDemandRepository
		);

		User writer = UserFixture.createUser(WRITER_ID, "서버", 36);
		meetingDemand = MeetingDemand.builder()
			.user(writer)
			.shortIntro("러닝 모임 열어주세요")
			.expectation("같이 꾸준히 달릴 수 있는 모임이 있으면 좋겠어요.")
			.meetingKeywordTypes(List.of(MeetingKeywordType.EXERCISE, MeetingKeywordType.NETWORKING))
			.joinInfo(new MeetingJoinInfo(MeetingType.ONLINE, MeetingFrequency.STEADY))
			.build();
		setField(meetingDemand, "id", MEETING_DEMAND_ID);
		setField(meetingDemand, "anonymousNickname", MEETING_DEMAND_ANONYMOUS_NICKNAME);
		setField(meetingDemand, "anonymousImageNumber", MEETING_DEMAND_ANONYMOUS_IMAGE_NUMBER);
	}

	@Test
	@DisplayName("게시글 작성자가 댓글 프로필을 처음 만들면 게시글 익명 프로필을 그대로 사용한다.")
	void findOrCreate_reusesMeetingDemandAnonymousProfileForWriter() {
		given(meetingDemandCommentProfileRepository.findByMeetingDemandIdAndUserId(MEETING_DEMAND_ID, WRITER_ID))
			.willReturn(Optional.empty());
		given(meetingDemandRepository.findByIdOrThrow(MEETING_DEMAND_ID)).willReturn(meetingDemand);
		given(meetingDemandCommentProfileRepository.saveAndFlush(any(MeetingDemandCommentProfile.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		MeetingDemandCommentProfile profile = meetingDemandCommentProfileFactory.findOrCreate(
			MEETING_DEMAND_ID, WRITER_ID);

		ArgumentCaptor<MeetingDemandCommentProfile> captor =
			ArgumentCaptor.forClass(MeetingDemandCommentProfile.class);
		verify(meetingDemandCommentProfileRepository).saveAndFlush(captor.capture());
		assertThat(profile).isSameAs(captor.getValue());
		assertThat(profile.getMeetingDemandId()).isEqualTo(MEETING_DEMAND_ID);
		assertThat(profile.getUserId()).isEqualTo(WRITER_ID);
		assertThat(profile.getAnonymousNickname()).isEqualTo(MEETING_DEMAND_ANONYMOUS_NICKNAME);
		assertThat(profile.getAnonymousImageNumber()).isEqualTo(MEETING_DEMAND_ANONYMOUS_IMAGE_NUMBER);
	}
}
