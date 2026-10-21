package org.sopt.makers.crew.main.entity.meeting;

import static org.sopt.makers.crew.main.global.exception.ErrorStatus.*;

import java.util.List;
import java.util.Optional;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingRepository extends JpaRepository<Meeting, Integer>, MeetingSearchRepository {

	/**
	 * @implSpec : 회식(DINING)은 회식 도메인에서만 다루므로 제외한다.
	 */
	default List<Meeting> findAllByUserId(Integer userId) {
		return findAllByUserIdAndCategoryNot(userId, MeetingCategory.DINING);
	}

	List<Meeting> findAllByUserIdAndCategoryNot(Integer userId, MeetingCategory category);

	/**
	 * @implSpec : 특정 유저가 모임장이거나 공동모임장인 모임을 최근에 만들어진 순으로 조회한다.
	 * 회식(DINING)은 제외한다.
	 **/
	default List<Meeting> findAllByUserIdOrIdInWithUser(Integer userId, List<Integer> coLeaderMeetingIds) {
		return findAllByUserIdOrIdInWithUserAndCategoryNot(userId, coLeaderMeetingIds, MeetingCategory.DINING);
	}

	@Query("SELECT m "
		+ "FROM Meeting m "
		+ "WHERE (m.userId =:userId "
		+ "OR m.id IN (:coLeaderMeetingIds)) "
		+ "AND m.category <> :excludedCategory "
		+ "ORDER BY m.id DESC ")
	List<Meeting> findAllByUserIdOrIdInWithUserAndCategoryNot(@Param("userId") Integer userId,
		@Param("coLeaderMeetingIds") List<Integer> coLeaderMeetingIds,
		@Param("excludedCategory") MeetingCategory excludedCategory);

	/**
	 * @implSpec : 기존 모임 API 용 단건 조회. 회식(DINING)은 존재하지 않는 모임으로 처리한다.
	 * 회식은 findDiningByIdOrThrow 로 조회한다.
	 */
	default Meeting findByIdOrThrow(Integer meetingId) {
		return findById(meetingId)
			.filter(meeting -> meeting.getCategory() != MeetingCategory.DINING)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_MEETING.getErrorCode()));
	}

	default Meeting findDiningByIdOrThrow(Integer meetingId) {
		return findById(meetingId)
			.filter(meeting -> meeting.getCategory() == MeetingCategory.DINING)
			.orElseThrow(() -> new NotFoundException(NOT_FOUND_DINING.getErrorCode()));
	}

	/**
	 * @implSpec : 회식(DINING)은 제외한다.
	 */
	default List<Meeting> findTop20ByOrderByIdDesc() {
		return findTop20ByCategoryNotOrderByIdDesc(MeetingCategory.DINING);
	}

	List<Meeting> findTop20ByCategoryNotOrderByIdDesc(MeetingCategory category);

	/**
	 * @implSpec : 내부 통계 API(기수별 모임 수)용. 회식(DINING)은 크루 모임 통계에 포함하지 않는다.
	 */
	default Integer countAllByCreatedGeneration(Integer generation) {
		return countAllByCreatedGenerationAndCategoryNot(generation, MeetingCategory.DINING);
	}

	Integer countAllByCreatedGenerationAndCategoryNot(Integer generation, MeetingCategory category);

	int countByMeetingDemandId(Integer meetingDemandId);

	Page<Meeting> findAllByMeetingDemandId(Integer meetingDemandId, Pageable pageable);

	@Modifying
	@Query("UPDATE Meeting m SET m.meetingDemandId = NULL WHERE m.meetingDemandId = :meetingDemandId")
	void clearMeetingDemandId(@Param("meetingDemandId") Integer meetingDemandId);

	/**
	 * @implSpec : 광고 배너(솝커톤 신청)의 연결 모임을 제목으로 찾는다.
	 * 같은 제목의 회식(DINING)이 연결되면 배너 클릭 시 404 가 나므로 회식은 제외한다.
	 */
	default Optional<Meeting> findFirstByTitleOrderByIdDesc(String title) {
		return findFirstByTitleAndCategoryNotOrderByIdDesc(title, MeetingCategory.DINING);
	}

	Optional<Meeting> findFirstByTitleAndCategoryNotOrderByIdDesc(String title, MeetingCategory category);

	/**
	 * @implSpec : 광고 배너(네트워킹 데이)의 연결 모임을 제목 포함 검색으로 찾는다.
	 * "[N기 네트워킹 데이] 뒤풀이" 같은 회식(DINING)이 연결되면 배너 클릭 시 404 가 나므로 회식은 제외한다.
	 */
	default Optional<Meeting> findFirstByTitleContainingOrderByIdDesc(String title) {
		return findFirstByTitleContainingAndCategoryNotOrderByIdDesc(title, MeetingCategory.DINING);
	}

	Optional<Meeting> findFirstByTitleContainingAndCategoryNotOrderByIdDesc(String title, MeetingCategory category);

	long countMeetingsByUserId(Integer userId);
}
