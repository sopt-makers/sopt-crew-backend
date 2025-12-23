package org.sopt.makers.crew.main.soptmap.service;

import static org.assertj.core.api.Assertions.*;

import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.entity.soptmap.MapTag;
import org.sopt.makers.crew.main.global.annotation.IntegratedTest;
import org.sopt.makers.crew.main.global.pagination.dto.PageOptionsDto;
import org.sopt.makers.crew.main.soptmap.dto.SortType;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapGetAllDto;
import org.sopt.makers.crew.main.soptmap.dto.response.SoptMapListResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
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
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.meta().getItemCount()).isEqualTo(25);
			assertThat(result.soptMaps()).hasSize(10);

			// 첫 번째 아이템 상세 검증
			SoptMapListResponseDto first = result.soptMaps().get(0);
			assertThat(first.getPlaceName()).isEqualTo("카페 온더플랜");
			assertThat(first.getSubwayStationNames()).containsExactlyInAnyOrder("강남역", "역삼역");
			assertThat(first.getRecommendCount()).isEqualTo(10L);
			assertThat(first.getIsRecommended()).isFalse();
		}

		@Test
		@DisplayName("시나리오 2: FOOD 카테고리로 필터링")
		void scenario2_filterByFood() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, MapTag.FOOD, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.meta().getItemCount()).isEqualTo(10);
			assertThat(result.soptMaps())
				.hasSize(10)
				.allMatch(dto -> dto.getMapTags().contains(MapTag.FOOD))
				.extracting("placeName")
				.containsExactlyInAnyOrder(
					"맛집 홍대점", "스시야 강남점", "파스타집", "한식당",
					"중식당", "일식당", "분식집", "치킨집",
					"브런치카페", "베이커리카페");
		}

		@Test
		@DisplayName("시나리오 3: CAFE 카테고리로 필터링")
		void scenario3_filterByCafe() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 12);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, MapTag.CAFE, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.meta().getItemCount()).isEqualTo(12);
			assertThat(result.soptMaps())
				.allMatch(dto -> dto.getMapTags().contains(MapTag.CAFE));
			assertThat(result.soptMaps())
				.extracting("placeName")
				.contains("카페 온더플랜", "스타벅스 역삼점", "브런치카페", "베이커리카페");
		}

		@Test
		@DisplayName("시나리오 4: ETC 카테고리로 필터링")
		void scenario4_filterByEtc() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, MapTag.ETC, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.meta().getItemCount()).isEqualTo(5);
			assertThat(result.soptMaps())
				.hasSize(5)
				.allMatch(dto -> dto.getMapTags().contains(MapTag.ETC))
				.extracting("placeName")
				.containsExactlyInAnyOrder("헬스장", "PC방", "노래방", "스터디카페", "코인세탁소");
		}

		@Test
		@DisplayName("시나리오 5: 추천순으로 정렬")
		void scenario5_sortByPopular() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, MapTag.FOOD, SortType.POPULAR, null, pageOptions);

			// then
			assertThat(result.soptMaps())
				.extracting(SoptMapListResponseDto::getRecommendCount)
				.isSortedAccordingTo(Comparator.reverseOrder());

			assertThat(result.soptMaps())
				.extracting("placeName")
				.startsWith("맛집 홍대점", "스시야 강남점", "파스타집");
		}

		@Test
		@DisplayName("시나리오 6: 페이지 간 데이터 일관성")
		void scenario6_paginationConsistency() {
			// given
			PageOptionsDto page1 = new PageOptionsDto(1, 10);
			PageOptionsDto page2 = new PageOptionsDto(2, 10);

			// when
			SoptMapGetAllDto firstPage = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, page1);
			SoptMapGetAllDto secondPage = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, page2);

			// then
			assertThat(firstPage.soptMaps()).hasSize(10);
			assertThat(secondPage.soptMaps()).hasSize(10);

			List<Long> firstPageIds = firstPage.soptMaps().stream()
				.map(SoptMapListResponseDto::getId)
				.toList();
			List<Long> secondPageIds = secondPage.soptMaps().stream()
				.map(SoptMapListResponseDto::getId)
				.toList();

			assertThat(firstPageIds).isNotEmpty();
			assertThat(secondPageIds).isNotEmpty();
			assertThat(firstPageIds).doesNotContainAnyElementsOf(secondPageIds);
		}

		@Test
		@DisplayName("시나리오 7: 카테고리 null이면 전체 조회")
		void scenario7_nullCategory_allResults() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 25);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.meta().getItemCount()).isEqualTo(25);

			boolean hasFood = result.soptMaps().stream()
				.anyMatch(dto -> dto.getMapTags().contains(MapTag.FOOD));
			boolean hasCafe = result.soptMaps().stream()
				.anyMatch(dto -> dto.getMapTags().contains(MapTag.CAFE));
			boolean hasEtc = result.soptMaps().stream()
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
		@DisplayName("다중 역이 있는 솝맵의 역 이름이 정확히 매핑된다")
		void multipleStations_mappedCorrectly() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			SoptMapListResponseDto onThePlan = result.soptMaps().stream()
				.filter(dto -> dto.getPlaceName().equals("카페 온더플랜"))
				.findFirst()
				.orElseThrow();

			assertThat(onThePlan.getSubwayStationNames())
				.hasSize(2)
				.containsExactlyInAnyOrder("강남역", "역삼역");
		}

		@Test
		@DisplayName("여러 솝맵의 역 정보가 각각 정확히 매핑된다")
		void multipleSoptMaps_stationsMappedCorrectly() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 25);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			SoptMapListResponseDto sushiya = result.soptMaps().stream()
				.filter(dto -> dto.getPlaceName().equals("스시야 강남점"))
				.findFirst()
				.orElseThrow();

			assertThat(sushiya.getSubwayStationNames())
				.containsExactlyInAnyOrder("강남역", "역삼역");

			SoptMapListResponseDto bakery = result.soptMaps().stream()
				.filter(dto -> dto.getPlaceName().equals("베이커리카페"))
				.findFirst()
				.orElseThrow();

			assertThat(bakery.getSubwayStationNames())
				.containsExactlyInAnyOrder("강남역", "건대입구역");
		}

		@Test
		@DisplayName("단일 역만 있는 솝맵도 정확히 매핑된다")
		void singleStation_mappedCorrectly() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 25);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			SoptMapListResponseDto hongdae = result.soptMaps().stream()
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
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.soptMaps()).hasSize(10);
			assertThat(result.soptMaps())
				.allMatch(dto -> dto.getSubwayStationNames() != null);

			assertThat(result.soptMaps())
				.filteredOn(dto -> !dto.getSubwayStationNames().isEmpty())
				.allMatch(dto -> dto.getSubwayStationNames().stream()
					.allMatch(name -> name != null && !name.isBlank()));
		}

		@Test
		@DisplayName("25개 전체 조회 시에도 역 정보가 정상 로드된다")
		void allStationsLoaded_25items() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 25);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.soptMaps()).hasSize(25);

			long withStations = result.soptMaps().stream()
				.filter(dto -> !dto.getSubwayStationNames().isEmpty())
				.count();

			assertThat(withStations).isGreaterThan(0);
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
			PageOptionsDto pageOptions = new PageOptionsDto(1, 25);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			SoptMapListResponseDto sushiya = result.soptMaps().stream()
				.filter(dto -> dto.getPlaceName().equals("스시야 강남점"))
				.findFirst()
				.orElseThrow();

			assertThat(sushiya.getIsRecommended()).isTrue();
			assertThat(sushiya.getRecommendCount()).isEqualTo(12L);
		}

		@Test
		@DisplayName("유저가 추천하지 않은 솝맵은 isRecommended가 false로 반환된다")
		void userNotRecommended_isFalse() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 25);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			SoptMapListResponseDto pasta = result.soptMaps().stream()
				.filter(dto -> dto.getPlaceName().equals("파스타집"))
				.findFirst()
				.orElseThrow();

			assertThat(pasta.getIsRecommended()).isFalse();
			assertThat(pasta.getRecommendCount()).isEqualTo(8L);
		}

		@Test
		@DisplayName("추천 정보와 역 정보가 모두 정확히 반환된다")
		void allInfoCorrect() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.POPULAR, null, pageOptions);

			// then
			SoptMapListResponseDto top = result.soptMaps().get(0);

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
			PageOptionsDto pageOptions = new PageOptionsDto(100, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then - 빈 페이지가 정상적으로 반환됨
			assertThat(result.soptMaps()).isEmpty();
		}

		@Test
		@DisplayName("userId가 null이어도 추천 정보는 모두 false로 반환된다")
		void nullUserId_allFalse() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				null, null, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.soptMaps())
				.isNotEmpty()
				.allMatch(dto -> dto.getIsRecommended() == false);
		}

		@Test
		@DisplayName("마지막 페이지의 아이템도 정상적으로 역 정보가 조회된다")
		void lastPage_stationsLoaded() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(3, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, null, pageOptions);

			// then
			assertThat(result.soptMaps()).hasSize(5);
			assertThat(result.soptMaps()).isNotEmpty();
			assertThat(result.soptMaps())
				.allMatch(dto -> dto.getSubwayStationNames() != null);
		}
	}

	@Nested
	@Sql(value = "/sql/soptmap-repository-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
	@Sql(value = "/sql/delete-all-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
	class 역_검색_기능 {

		@Test
		@DisplayName("역 검색어가 있으면 해당 역 근처 솝맵만 반환된다")
		void filterByStationKeyword_success() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, "강남", pageOptions);

			// then
			assertThat(result.soptMaps()).isNotEmpty();
			assertThat(result.soptMaps())
				.allMatch(dto -> dto.getSubwayStationNames().stream()
					.anyMatch(name -> name.contains("강남")));
		}

		@Test
		@DisplayName("존재하지 않는 역 검색 시 빈 결과 반환")
		void stationNotFound_emptyResult() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, null, SortType.LATEST, "존재하지않는역", pageOptions);

			// then
			assertThat(result.soptMaps()).isEmpty();
			assertThat(result.meta().getItemCount()).isZero();
		}

		@Test
		@DisplayName("역 검색과 카테고리 필터를 함께 사용할 수 있다")
		void stationAndCategoryFilter_success() {
			// given
			PageOptionsDto pageOptions = new PageOptionsDto(1, 10);

			// when
			SoptMapGetAllDto result = soptMapService.getSoptMapList(
				1, MapTag.FOOD, SortType.LATEST, "강남", pageOptions);

			// then
			assertThat(result.soptMaps()).isNotEmpty();
			assertThat(result.soptMaps())
				.allMatch(dto -> dto.getMapTags().contains(MapTag.FOOD))
				.allMatch(dto -> dto.getSubwayStationNames().stream()
					.anyMatch(name -> name.contains("강남")));
		}
	}
}
