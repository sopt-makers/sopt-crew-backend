package org.sopt.makers.crew.main.entity.apply;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyStatus;
import org.sopt.makers.crew.main.entity.apply.enums.EnApplyType;
import org.sopt.makers.crew.main.entity.meeting.Meeting;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.entity.meeting.enums.MeetingJoinablePart;
import org.sopt.makers.crew.main.entity.user.User;
import org.sopt.makers.crew.main.entity.user.vo.UserActivityVO;

public class AppliesTest {

	@Test
	void 승인된_신청자_수_조회(){
		// given
		User host = User.builder().name("김철수")
			.orgId(1)
			.activities(List.of(new UserActivityVO("웹", 31)))
			.profileImage(null)
			.phone("010-2222-2222")
			.build();

		Meeting meeting = Meeting.builder()
			.user(host)
			.title("모임 제목입니다")
			.category(MeetingCategory.EVENT)
			.startDate(LocalDateTime.of(2024,4,24,0,0,0))
			.endDate(LocalDateTime.of(2024,4,29,23,59,59))
			.capacity(20)
			.desc("모임 소개입니다")
			.processDesc("진행방식 소개입니다")
			.mStartDate(LocalDateTime.of(2024,5,24,0,0,0))
			.mEndDate(LocalDateTime.of(2024,6,24,0,0,0))
			.leaderDesc("스장 소개입니다")
			.isMentorNeeded(true)
			.canJoinOnlyActiveGeneration(false)
			.createdGeneration(34)
			.targetActiveGeneration(null)
			.joinableParts(MeetingJoinablePart.values())
			.build();

		User applicant1 = User.builder().name("홍길동")
			.orgId(2)
			.activities(List.of(new UserActivityVO("서버", 33)))
			.profileImage(null)
			.phone("010-1234-5678")
			.build();

		User applicant2 = User.builder().name("김삼순")
			.orgId(3)
			.activities(List.of(new UserActivityVO("디자인", 34)))
			.profileImage(null)
			.phone("010-1111-1111")
			.build();

		Apply apply1 = Apply.builder()
			.type(EnApplyType.APPLY)
			.user(applicant1)
			.userId(2)
			.content("지원동기입니다1")
			.meeting(meeting)
			.meetingId(1)
			.build();

		Apply apply2 = Apply.builder()
			.type(EnApplyType.APPLY)
			.user(applicant2)
			.userId(3)
			.content("지원동기입니다2")
			.meeting(meeting)
			.meetingId(1)
			.build();
		apply2.updateApplyStatus(EnApplyStatus.APPROVE);

		Applies applyGroups = new Applies(List.of(apply1, apply2));

		// when
		long approvedCount = applyGroups.getApprovedCount(1);

		// then
		Assertions.assertThat(approvedCount).isEqualTo(1L);
	}

	@Test
	void 특정_사용자가_신청했는지_조회(){
		// given
		User host = User.builder().name("김철수")
			.orgId(1)
			.activities(List.of(new UserActivityVO("웹", 31)))
			.profileImage(null)
			.phone("010-2222-2222")
			.build();

		Meeting meeting = Meeting.builder()
			.user(host)
			.title("모임 제목입니다")
			.category(MeetingCategory.EVENT)
			.startDate(LocalDateTime.of(2024,4,24,0,0,0))
			.endDate(LocalDateTime.of(2024,4,29,23,59,59))
			.capacity(20)
			.desc("모임 소개입니다")
			.processDesc("진행방식 소개입니다")
			.mStartDate(LocalDateTime.of(2024,5,24,0,0,0))
			.mEndDate(LocalDateTime.of(2024,6,24,0,0,0))
			.leaderDesc("스장 소개입니다")
			.isMentorNeeded(true)
			.canJoinOnlyActiveGeneration(false)
			.createdGeneration(34)
			.targetActiveGeneration(null)
			.joinableParts(MeetingJoinablePart.values())
			.build();

		User applicant1 = User.builder().name("홍길동")
			.orgId(2)
			.activities(List.of(new UserActivityVO("서버", 33)))
			.profileImage(null)
			.phone("010-1234-5678")
			.build();

		Apply apply1 = Apply.builder()
			.type(EnApplyType.APPLY)
			.user(applicant1)
			.userId(2)
			.content("지원동기입니다1")
			.meeting(meeting)
			.meetingId(1)
			.build();

		Applies applies = new Applies(List.of(apply1));

		// when
		Boolean isApply = applies.isApply(1,2);

		// then
		Assertions.assertThat(isApply).isEqualTo(true);
	}

	@Test
	void 특정_사용자가_승인되었는지_조회(){
		// given
		User host = User.builder().name("김철수")
			.orgId(1)
			.activities(List.of(new UserActivityVO("웹", 31)))
			.profileImage(null)
			.phone("010-2222-2222")
			.build();

		Meeting meeting = Meeting.builder()
			.user(host)
			.title("모임 제목입니다")
			.category(MeetingCategory.EVENT)
			.startDate(LocalDateTime.of(2024,4,24,0,0,0))
			.endDate(LocalDateTime.of(2024,4,29,23,59,59))
			.capacity(20)
			.desc("모임 소개입니다")
			.processDesc("진행방식 소개입니다")
			.mStartDate(LocalDateTime.of(2024,5,24,0,0,0))
			.mEndDate(LocalDateTime.of(2024,6,24,0,0,0))
			.leaderDesc("스장 소개입니다")
			.isMentorNeeded(true)
			.canJoinOnlyActiveGeneration(false)
			.createdGeneration(34)
			.targetActiveGeneration(null)
			.joinableParts(MeetingJoinablePart.values())
			.build();

		User applicant1 = User.builder().name("홍길동")
			.orgId(2)
			.activities(List.of(new UserActivityVO("서버", 33)))
			.profileImage(null)
			.phone("010-1234-5678")
			.build();

		User applicant2 = User.builder().name("김삼순")
			.orgId(3)
			.activities(List.of(new UserActivityVO("디자인", 34)))
			.profileImage(null)
			.phone("010-1111-1111")
			.build();

		Apply apply1 = Apply.builder()
			.type(EnApplyType.APPLY)
			.user(applicant1)
			.userId(2)
			.content("지원동기입니다1")
			.meeting(meeting)
			.meetingId(1)
			.build();

		Apply apply2 = Apply.builder()
			.type(EnApplyType.APPLY)
			.user(applicant2)
			.userId(3)
			.content("지원동기입니다2")
			.meeting(meeting)
			.meetingId(1)
			.build();
		apply2.updateApplyStatus(EnApplyStatus.APPROVE);

		Applies applies = new Applies(List.of(apply1, apply2));

		// when
		Boolean isApproved1 = applies.isApproved(1,2);
		Boolean isApproved2 = applies.isApproved(1,3);

		// then
		Assertions.assertThat(isApproved1).isEqualTo(false);
		Assertions.assertThat(isApproved2).isEqualTo(true);
	}
}
