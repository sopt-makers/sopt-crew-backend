package org.sopt.makers.crew.main.soptmap.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapListResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@IntegratedTest
public class SoptMapServiceTest {

	@Autowired
	private SoptMapService soptMapService;

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 기획_시나리오_테스트 {

		@Test
		@DisplayName("시나리오 1: 첫 화면 진입 - 전체/최신순 디폴트")
		void scenario1_initialLoad() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(25);
			assertThat(result.getContent()).hasSize(10);

			// 첫 번째 아이템 상세 검증
			SoptMapListResponseDto first = result.getContent().get(0);
			assertThat(first.getPlaceName()).isEqualTo("카페 온더플랜");
			assertThat(first.getSubwayStationNames()).containsExactlyInAnyOrder("강남역", "역삼역");
			assertThat(first.getRecommendCount()).isEqualTo(10L);
			assertThat(first.getIsRecommended()).isFalse(); // userId=1이 추천 안함
		}

		@Test
		@DisplayName("시나리오 2: 음식점 카테고리 선택")
		void scenario2_foodCategory() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, MapTag.FOOD, SortType.LATEST, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(10); // FOOD(8) + 복합(2)
			assertThat(result.getContent())
				.hasSize(10)
				.allMatch(dto -> dto.getMapTags().contains(MapTag.FOOD))
				.extracting("placeName")
				.containsExactlyInAnyOrder(
					"맛집 홍대점", "스시야 강남점", "파스타집", "한식당",
					"중식당", "일식당", "분식집", "치킨집",
					"브런치카페", "베이커리카페");
		}

		@Test
		@DisplayName("시나리오 5: 카페 카테고리 선택")
		void scenario5_cafeCategory() {
			// given
			PageRequest pageable = PageRequest.of(0, 15);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, MapTag.CAFE, SortType.LATEST, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(12); // CAFE(10) + 복합(2)
			assertThat(result.getContent())
				.allMatch(dto -> dto.getMapTags().contains(MapTag.CAFE));
			assertThat(result.getContent())
				.extracting("placeName")
				.contains("카페 온더플랜", "스타벅스 역삼점", "브런치카페", "베이커리카페");
		}

		@Test
		@DisplayName("시나리오 6: 기타 카테고리 선택")
		void scenario6_etcCategory() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, MapTag.ETC, SortType.LATEST, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(5); // ETC만
			assertThat(result.getContent())
				.hasSize(5)
				.allMatch(dto -> dto.getMapTags().contains(MapTag.ETC))
				.extracting("placeName")
				.containsExactlyInAnyOrder("헬스장", "PC방", "노래방", "스터디카페", "코인세탁소");
		}

		@Test
		@DisplayName("시나리오 3: 추천순으로 정렬 변경")
		void scenario3_sortByPopular() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, MapTag.FOOD, SortType.POPULAR, pageable);

			// then
			assertThat(result.getContent())
				.extracting(SoptMapListResponseDto::getRecommendCount)
				.isSortedAccordingTo(Comparator.reverseOrder());

			assertThat(result.getContent())
				.extracting("placeName")
				.startsWith("맛집 홍대점", "스시야 강남점", "파스타집"); // 추천 많은 순
		}

		@Test
		@DisplayName("시나리오 4: 두 번째 페이지 조회")
		void scenario4_secondPage() {
			// given
			PageRequest page1 = PageRequest.of(0, 10);
			PageRequest page2 = PageRequest.of(1, 10);

			// when
			Page<SoptMapListResponseDto> firstPage = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, page1);
			Page<SoptMapListResponseDto> secondPage = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, page2);

			// then
			assertThat(firstPage.getContent()).hasSize(10);
			assertThat(secondPage.getContent()).hasSize(10);

			// 첫 페이지와 두 번째 페이지는 겹치지 않음
			List<Long> firstPageIds = firstPage.getContent().stream()
				.map(SoptMapListResponseDto::getId)
				.toList();
			List<Long> secondPageIds = secondPage.getContent().stream()
				.map(SoptMapListResponseDto::getId)
				.toList();

			assertThat(firstPageIds).isNotEmpty();
			assertThat(secondPageIds).isNotEmpty();
			assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
		}

		@Test
		@DisplayName("시나리오 7: 전체 복귀")
		void scenario7_returnToAll() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			assertThat(result.getTotalElements()).isEqualTo(25); // 모든 카테고리

			// 각 카테고리가 최소 하나씩 있는지 확인
			boolean hasFood = result.getContent().stream()
				.anyMatch(dto -> dto.getMapTags().contains(MapTag.FOOD));
			boolean hasCafe = result.getContent().stream()
				.anyMatch(dto -> dto.getMapTags().contains(MapTag.CAFE));
			boolean hasEtc = result.getContent().stream()
				.anyMatch(dto -> dto.getMapTags().contains(MapTag.ETC));

			assertThat(hasFood).isTrue();
			assertThat(hasCafe).isTrue();
			assertThat(hasEtc).isTrue();
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 지하철역_매핑 {

		@Test
		@DisplayName("nearbyStationIds를 역 이름으로 변환한다")
		void mapStationIdsToNames_success() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			SoptMapListResponseDto onThePlan = result.getContent().stream()
				.filter(dto -> dto.getPlaceName().equals("카페 온더플랜"))
				.findFirst()
				.orElseThrow();

			assertThat(onThePlan.getSubwayStationNames())
				.hasSize(2)
				.containsExactlyInAnyOrder("강남역", "역삼역");
		}

		@Test
		@DisplayName("복수의 역이 정확히 매핑된다")
		void multipleStations_mapped() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			SoptMapListResponseDto sushiya = result.getContent().stream()
				.filter(dto -> dto.getPlaceName().equals("스시야 강남점"))
				.findFirst()
				.orElseThrow();

			assertThat(sushiya.getSubwayStationNames())
				.containsExactlyInAnyOrder("강남역", "역삼역");

			SoptMapListResponseDto bakery = result.getContent().stream()
				.filter(dto -> dto.getPlaceName().equals("베이커리카페"))
				.findFirst()
				.orElseThrow();

			assertThat(bakery.getSubwayStationNames())
				.containsExactlyInAnyOrder("강남역", "건대입구역");
		}

		@Test
		@DisplayName("단일 역만 있는 경우도 정상 매핑된다")
		void singleStation_mapped() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			SoptMapListResponseDto hongdae = result.getContent().stream()
				.filter(dto -> dto.getPlaceName().equals("맛집 홍대점"))
				.findFirst()
				.orElseThrow();

			assertThat(hongdae.getSubwayStationNames())
				.hasSize(1)
				.containsExactly("홍대입구역");
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 지하철역_정보_검증 {

		@Test
		@DisplayName("10개 조회 시 모든 역 정보가 정상 조회된다")
		void allStationsLoaded_10items() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			// 모든 역 이름이 채워져 있음
			assertThat(result.getContent()).hasSize(10);
			assertThat(result.getContent())
				.allMatch(dto -> dto.getSubwayStationNames() != null);

			// 역이 있는 모든 SoptMap은 역 이름이 올바르게 매핑됨
			assertThat(result.getContent())
				.filteredOn(dto -> !dto.getSubwayStationNames().isEmpty())
				.allMatch(dto -> dto.getSubwayStationNames().stream()
					.allMatch(name -> name != null && !name.isBlank()));
		}

		@Test
		@DisplayName("25개 전체 조회 시에도 역 정보가 정상 로드된다")
		void allStationsLoaded_25items() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			assertThat(result.getContent()).hasSize(25);

			// 모든 데이터의 역 이름이 정상적으로 조회됨
			long withStations = result.getContent().stream()
				.filter(dto -> !dto.getSubwayStationNames().isEmpty())
				.count();

			assertThat(withStations).isGreaterThan(0); // 역 정보가 있는 데이터가 존재
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 추천_정보_검증 {

		@Test
		@DisplayName("유저가 추천한 솝맵은 isRecommended가 true로 반환된다")
		void userRecommended_isTrue() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			SoptMapListResponseDto sushiya = result.getContent().stream()
				.filter(dto -> dto.getPlaceName().equals("스시야 강남점"))
				.findFirst()
				.orElseThrow();

			assertThat(sushiya.getIsRecommended()).isTrue(); // userId=1이 추천함
			assertThat(sushiya.getRecommendCount()).isEqualTo(12L);
		}

		@Test
		@DisplayName("유저가 추천하지 않은 솝맵은 isRecommended가 false로 반환된다")
		void userNotRecommended_isFalse() {
			// given
			PageRequest pageable = PageRequest.of(0, 25);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			SoptMapListResponseDto pasta = result.getContent().stream()
				.filter(dto -> dto.getPlaceName().equals("파스타집"))
				.findFirst()
				.orElseThrow();

			assertThat(pasta.getIsRecommended()).isFalse(); // userId=1이 추천 안함
			assertThat(pasta.getRecommendCount()).isEqualTo(8L);
		}

		@Test
		@DisplayName("추천 정보와 역 정보가 모두 정확히 반환된다")
		void allInfoCorrect() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.POPULAR, pageable);

			// then
			SoptMapListResponseDto top = result.getContent().get(0);

			assertThat(top)
				.extracting("placeName", "recommendCount", "isRecommended")
				.containsExactly("맛집 홍대점", 15L, true);

			assertThat(top.getSubwayStationNames())
				.containsExactly("홍대입구역");

			assertThat(top.getMapTags())
				.contains(MapTag.FOOD);

			assertThat(top.getDescription())
				.isNotNull()
				.isNotBlank();
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class Edge_Cases {

		@Test
		@DisplayName("빈 결과 페이지를 정상 처리한다")
		void emptyPage_handled() {
			// given - 존재하지 않는 페이지 요청
			PageRequest pageable = PageRequest.of(100, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then - 빈 페이지가 정상적으로 반환됨
			assertThat(result.getContent()).isEmpty();
			assertThat(result.hasContent()).isFalse();
			// totalElements는 실제 데이터 수에 따라 달라지므로 제외
		}

		@Test
		@DisplayName("userId가 null이어도 추천 정보는 모두 false로 반환된다")
		void nullUserId_allFalse() {
			// given
			PageRequest pageable = PageRequest.of(0, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				null, null, SortType.LATEST, pageable);

			// then
			assertThat(result.getContent())
				.allMatch(dto -> dto.getIsRecommended() == false);
		}

		@Test
		@DisplayName("마지막 페이지의 아이템도 정상적으로 역 정보가 조회된다")
		void lastPage_stationsLoaded() {
			// given
			PageRequest pageable = PageRequest.of(2, 10);

			// when
			Page<SoptMapListResponseDto> result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, pageable);

			// then
			assertThat(result.getContent()).hasSize(5);
			assertThat(result.getContent()).isNotEmpty();
			assertThat(result.getContent())
				.allMatch(dto -> dto.getSubwayStationNames() != null);
		}
	}
}
