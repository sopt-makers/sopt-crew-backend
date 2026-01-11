package org.sopt.makers.crew.main.soptmap.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.entity.soptmap.repository.SoptMapRepository;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.service.dto.SoptMapWithRecommendInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@IntegratedTest
class SoptMapRepositoryTest {

	@Autowired
	private SoptMapRepository soptMapRepository;

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 전체_조회 {

		@Test
		@DisplayName("카테고리 필터 없이 전체 조회 시, 모든 솝맵이 최신순으로 반환된다")
		void searchAllWithLatestSort_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(25);
			assertThat(result.getContent())
				.hasSize(10)
				.extracting("placeName")
				.containsExactly(
					"카페 온더플랜", // 18:00 (최신)
					"스타벅스 역삼점", // 17:30
					"맛집 홍대점", // 17:00
					"스시야 강남점", // 16:00
					"파스타집", // 15:00
					"한식당", // 14:00
					"중식당", // 13:00
					"일식당", // 12:00
					"분식집", // 11:00
					"치킨집" // 10:00
				);
		}

		@Test
		@DisplayName("전체 조회 시, 추천 수가 정확히 집계된다")
		void searchAll_recommendCountCorrect() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			SoptMapWithRecommendInfo restaurant = result.getContent().stream()
				.filter(info -> info.getPlaceName().equals("맛집 홍대점"))
				.findFirst()
				.orElseThrow();

			assertThat(restaurant.getRecommendCount()).isEqualTo(15L); // 비활성(active=false) 제외
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 카테고리_필터 {

		@Test
		@DisplayName("FOOD 카테고리 필터링 시, FOOD 태그가 있는 솝맵만 반환된다")
		void filterByFood_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 20);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, List.of(MapTag.FOOD), SortType.LATEST, null, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(10); // FOOD(8) + 복합(2)
			assertThat(result.getContent())
				.hasSize(10)
				.allMatch(info -> info.getMapTags().contains(MapTag.FOOD))
				.extracting("placeName")
				.containsExactlyInAnyOrder(
					"맛집 홍대점", "스시야 강남점", "파스타집", "한식당",
					"중식당", "일식당", "분식집", "치킨집",
					"브런치카페", "베이커리카페" // 복합 카테고리 포함
				);
		}

		@Test
		@DisplayName("CAFE 카테고리 필터링 시, CAFE 태그가 있는 솝맵만 반환된다")
		void filterByCafe_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 20);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, List.of(MapTag.CAFE), SortType.LATEST, null, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(12); // CAFE(10) + 복합(2)
			assertThat(result.getContent())
				.allMatch(info -> info.getMapTags().contains(MapTag.CAFE));
		}

		@Test
		@DisplayName("ETC 카테고리 필터링 시, ETC 태그가 있는 솝맵만 반환된다")
		void filterByEtc_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 20);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, List.of(MapTag.ETC), SortType.LATEST, null, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(5); // ETC만
			assertThat(result.getContent())
				.allMatch(info -> info.getMapTags().contains(MapTag.ETC))
				.extracting("placeName")
				.contains("헬스장", "PC방", "노래방", "스터디카페", "코인세탁소");
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 추천순_정렬 {

		@Test
		@DisplayName("POPULAR 정렬 시, 추천 수가 많은 순으로 반환된다")
		void sortByPopular_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.POPULAR, null, pageable);

			// then
			assertThat(result.getContent())
				.extracting("placeName", "recommendCount")
				.containsExactly(
					tuple("맛집 홍대점", 15L),
					tuple("스시야 강남점", 12L),
					tuple("카페 온더플랜", 10L),
					tuple("파스타집", 8L),
					tuple("한식당", 5L),
					tuple("스타벅스 역삼점", 3L),
					tuple("브런치카페", 2L),
					tuple("중식당", 0L), // 추천 0개는 최신순
					tuple("일식당", 0L),
					tuple("분식집", 0L));
		}

		@Test
		@DisplayName("추천 수가 같으면 최신순으로 정렬된다")
		void sameRecommendCount_sortByLatest() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.POPULAR, null, pageable);

			// then
			List<SoptMapWithRecommendInfo> zeroRecommends = result.getContent().stream()
				.filter(info -> info.getRecommendCount() == 0L)
				.toList();

			// 추천 0개인 항목들이 시간 역순으로 정렬되는지 확인
			assertThat(zeroRecommends)
				.hasSize(18)
				.extracting("placeName")
				.containsExactly(
					"중식당", "일식당", "분식집", "치킨집",
					"투썸플레이스", "할리스커피", "카페베네", "엔제리너스",
					"빽다방", "메가커피", "컴포즈커피", "이디야커피",
					"헬스장", "PC방", "노래방", "스터디카페",
					"코인세탁소", "베이커리카페");
		}

		@Test
		@DisplayName("FOOD 카테고리 필터 + 추천순 정렬이 함께 동작한다")
		void filterAndSort_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, List.of(MapTag.FOOD), SortType.POPULAR, null, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(10);
			assertThat(result.getContent())
				.allMatch(info -> info.getMapTags().contains(MapTag.FOOD))
				.extracting("placeName", "recommendCount")
				.startsWith(
					tuple("맛집 홍대점", 15L),
					tuple("스시야 강남점", 12L),
					tuple("파스타집", 8L));
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 최신순_정렬 {

		@Test
		@DisplayName("LATEST 정렬 시, 최신 순으로 정렬된다")
		void sortByLatest_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 5);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getContent())
				.extracting("placeName")
				.containsExactly(
					"카페 온더플랜", // 18:00
					"스타벅스 역삼점", // 17:30
					"맛집 홍대점", // 17:00
					"스시야 강남점", // 16:00
					"파스타집" // 15:00
				);
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 추천_정보 {

		@Test
		@DisplayName("현재 유저가 추천한 솝맵은 isRecommended가 true다")
		void userRecommended_isTrue() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			SoptMapWithRecommendInfo sushiya = result.getContent().stream()
				.filter(info -> info.getPlaceName().equals("스시야 강남점"))
				.findFirst()
				.orElseThrow();

			assertThat(sushiya.getIsRecommended()).isTrue();
			assertThat(sushiya.getRecommendCount()).isEqualTo(12L);
		}

		@Test
		@DisplayName("현재 유저가 추천하지 않은 솝맵은 isRecommended가 false다")
		void userNotRecommended_isFalse() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			SoptMapWithRecommendInfo pasta = result.getContent().stream()
				.filter(info -> info.getPlaceName().equals("파스타집"))
				.findFirst()
				.orElseThrow();

			assertThat(pasta.getIsRecommended()).isFalse(); // userId=1이 추천 안함
			assertThat(pasta.getRecommendCount()).isEqualTo(8L);
		}

		@Test
		@DisplayName("추천이 없는 솝맵은 recommendCount가 0이다")
		void noRecommend_countIsZero() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			List<SoptMapWithRecommendInfo> noRecommends = result.getContent().stream()
				.filter(info -> info.getRecommendCount() == 0L)
				.toList();

			assertThat(noRecommends)
				.isNotEmpty()
				.allMatch(info -> info.getIsRecommended() == false);
		}

		@Test
		@DisplayName("비활성(active=false) 추천은 집계되지 않는다")
		void inactiveRecommend_notCounted() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			SoptMapWithRecommendInfo restaurant = result.getContent().stream()
				.filter(info -> info.getPlaceName().equals("맛집 홍대점"))
				.findFirst()
				.orElseThrow();

			// 테스트 데이터: 활성 15개 + 비활성 1개 = 총 16개
			// 하지만 active=true만 집계되므로 15개
			assertThat(restaurant.getRecommendCount()).isEqualTo(15L);
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 페이지네이션 {

		@Test
		@DisplayName("첫 페이지 조회 시, 첫 10개가 반환된다")
		void firstPage_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getPageable().getPageNumber()).isZero();
			assertThat(result.getContent()).hasSize(10);
			assertThat(result.getTotalElements()).isEqualTo(25);
			assertThat(result.getTotalPages()).isEqualTo(3);
		}

		@Test
		@DisplayName("두 번째 페이지 조회 시, 11~20번째 데이터가 반환된다")
		void secondPage_success() {
			// given
			PageRequest pageable = PageRequest.of(1, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getPageable().getPageNumber()).isEqualTo(1);
			assertThat(result.getContent()).hasSize(10);
			assertThat(result.getContent())
				.extracting("placeName")
				.contains("투썸플레이스", "할리스커피");
		}

		@Test
		@DisplayName("마지막 페이지 조회 시, 남은 개수만 반환된다")
		void lastPage_success() {
			// given
			PageRequest pageable = PageRequest.of(2, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getPageable().getPageNumber()).isEqualTo(2);
			assertThat(result.getContent()).hasSize(5); // 25개 중 마지막 5개
			assertThat(result.isLast()).isTrue();
		}

		@Test
		@DisplayName("페이지 크기 20으로 조회 시, 20개가 반환된다")
		void pageSize20_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 20);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getContent()).hasSize(20);
			assertThat(result.getTotalPages()).isEqualTo(2); // 25 ÷ 20 = 2페이지
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class Edge_Cases {

		@Test
		@DisplayName("존재하지 않는 유저 ID로 조회해도 정상 동작한다")
		void nonExistentUser_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				99999L, null, SortType.LATEST, null, pageable);

			// then
			assertThat(result.getContent()).hasSize(10);
			assertThat(result.getContent())
				.allMatch(info -> info.getIsRecommended() == false); // 모든 추천이 false
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 역_필터링 {

		@Test
		@DisplayName("강남역 ID로 필터링 시, 강남역 근처 솝맵만 반환된다")
		void filterByGangnamStation_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 20);
			List<Long> gangnamStationIds = List.of(1L); // 강남역

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, gangnamStationIds, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(10); // 강남역 근처 10개
			assertThat(result.getContent())
				.extracting("placeName")
				.contains(
					"카페 온더플랜", "스시야 강남점", "파스타집", "일식당",
					"투썸플레이스", "엔제리너스", "컴포즈커피", "헬스장",
					"스터디카페", "베이커리카페");
		}

		@Test
		@DisplayName("여러 역 ID로 필터링 시, 해당 역들 근처 솝맵이 OR 조건으로 반환된다")
		void filterByMultipleStationIds_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);
			List<Long> stationIds = List.of(1L, 2L); // 강남역(1), 역삼역(2)

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, stationIds, pageable);

			// then - 강남역(10개) + 역삼역 only(7개) = 17개 (중복 제거)
			assertThat(result.getTotalElements()).isEqualTo(17);
		}

		@Test
		@DisplayName("역 필터 + 카테고리 필터가 AND 조건으로 함께 동작한다")
		void filterByStationAndCategory_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);
			List<Long> gangnamStationIds = List.of(1L); // 강남역

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, List.of(MapTag.FOOD), SortType.LATEST, gangnamStationIds, pageable);

			// then
			assertThat(result.getContent())
				.allMatch(info -> info.getMapTags().contains(MapTag.FOOD))
				.extracting("placeName")
				.contains("스시야 강남점", "파스타집", "일식당", "베이커리카페");
		}

		@Test
		@DisplayName("역 필터 + 추천순 정렬이 함께 동작한다")
		void filterByStationAndSortByPopular_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);
			List<Long> gangnamStationIds = List.of(1L); // 강남역

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.POPULAR, gangnamStationIds, pageable);

			// then
			assertThat(result.getContent())
				.extracting("placeName", "recommendCount")
				.startsWith(
					tuple("스시야 강남점", 12L),
					tuple("카페 온더플랜", 10L),
					tuple("파스타집", 8L));
		}

		@Test
		@DisplayName("존재하지 않는 역 ID로 필터링하면 빈 결과가 반환된다")
		void filterByNonExistentStationId_returnsEmpty() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);
			List<Long> nonExistentStationIds = List.of(99999L);

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, nonExistentStationIds, pageable);

			// then
			assertThat(result.getContent()).isEmpty();
			assertThat(result.getTotalElements()).isZero();
		}

		@Test
		@DisplayName("빈 역 ID 리스트는 null과 동일하게 처리되어 전체 결과를 반환한다")
		void filterByEmptyStationIds_returnsAll() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);
			List<Long> emptyStationIds = List.of();

			// when
			Page<SoptMapWithRecommendInfo> result = soptMapRepository.searchSoptMap(
				1L, null, SortType.LATEST, emptyStationIds, pageable);

			// then
			assertThat(result.getContent()).hasSize(10);
			assertThat(result.getTotalElements()).isEqualTo(25); // 전체 결과
		}
	}
}
