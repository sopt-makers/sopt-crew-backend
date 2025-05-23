package org.sopt.makers.crew.main.entity.user;

import java.util.Collections;
import java.util.List;

import org.sopt.makers.crew.main.entity.tag.enums.MeetingKeywordType;
import org.sopt.makers.crew.main.external.notification.vo.KeywordMatchedUserDto;
import org.sopt.makers.crew.main.global.dto.MeetingCreatorDto;
import org.sopt.makers.crew.main.global.dto.OrgIdListDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {
	private final UserRepository userRepository;

	@Cacheable(value = "meetingLeaderCache", key = "#userId")
	public MeetingCreatorDto getMeetingLeader(Integer userId) {
		return MeetingCreatorDto.from(userRepository.findByIdOrThrow(userId));
	}

	@Cacheable(value = "orgIdCache", key = "'allOrgIds'")
	public OrgIdListDto findAllOrgIds() {
		return OrgIdListDto.of(userRepository.findAllOrgIds());
	}

	public List<KeywordMatchedUserDto> findByInterestingKeywordTypes(List<String> meetingKeywordTypes) {
		List<User> allUsers = userRepository.findAll();

		List<MeetingKeywordType> meetingKeywords = meetingKeywordTypes.stream()
			.map(MeetingKeywordType::ofValue)
			.toList();

		return allUsers.stream()
			.filter(u -> !Collections.disjoint(u.getInterestedKeywords(), meetingKeywords)
			).map(KeywordMatchedUserDto::of).toList();
	}

}
