package org.sopt.makers.crew.main.auth.v2.service;

import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sopt.makers.crew.main.entity.meeting.CoLeader;
import org.sopt.makers.crew.main.entity.meeting.CoLeaderRepository;
import org.sopt.makers.crew.main.entity.meeting.Meeting;

@ExtendWith(MockitoExtension.class)
class AuthV2UserCacheInvalidationServiceTest {

	@InjectMocks
	private AuthV2UserCacheInvalidationServiceImpl authV2UserCacheInvalidationService;

	@Mock
	private CoLeaderRepository coLeaderRepository;

	@Mock
	private AuthV2UserCacheCommandService authV2UserCacheCommandService;

	@Test
	@DisplayName("유저 정보 변경 시 leader, coLeader, orgId cache 갱신 테스트")
	void 유저_정보_변경시_leader_coLeader_orgId_cache를_모두_갱신한다() {
		Meeting firstMeeting = mock(Meeting.class);
		Meeting secondMeeting = mock(Meeting.class);
		CoLeader firstCoLeader = mock(CoLeader.class);
		CoLeader secondCoLeader = mock(CoLeader.class);
		CoLeader duplicatedCoLeader = mock(CoLeader.class);

		doReturn(100).when(firstMeeting).getId();
		doReturn(200).when(secondMeeting).getId();
		doReturn(firstMeeting).when(firstCoLeader).getMeeting();
		doReturn(secondMeeting).when(secondCoLeader).getMeeting();
		doReturn(firstMeeting).when(duplicatedCoLeader).getMeeting();
		doReturn(List.of(firstCoLeader, secondCoLeader, duplicatedCoLeader))
			.when(coLeaderRepository).findAllByUserIdWithMeeting(1);

		authV2UserCacheInvalidationService.refreshCachesAfterUserUpdate(1);

		verify(authV2UserCacheCommandService).evictMeetingLeaderCache(1);
		verify(authV2UserCacheCommandService).evictCoLeadersCache(100);
		verify(authV2UserCacheCommandService).evictCoLeadersCache(200);
		verify(authV2UserCacheCommandService, times(1)).evictCoLeadersCache(100);
		verify(authV2UserCacheCommandService).refreshOrgIdCache();
	}

	@Test
	@DisplayName("신규 유저 생성 시 orgId cache 갱신 테스트")
	void 신규_유저_생성시_orgId_cache만_갱신한다() {
		authV2UserCacheInvalidationService.refreshCachesAfterUserCreate();

		verify(authV2UserCacheCommandService).refreshOrgIdCache();
		verifyNoInteractions(coLeaderRepository);
		verify(authV2UserCacheCommandService, never()).evictMeetingLeaderCache(anyInt());
		verify(authV2UserCacheCommandService, never()).evictCoLeadersCache(anyInt());
	}
}
