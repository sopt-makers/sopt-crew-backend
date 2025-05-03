package org.sopt.makers.crew.main.external;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sopt.makers.crew.main.external.caffeine.CacheQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = CacheQueryService.class)
@Import(value = CaffeineTestConfig.class)
@ActiveProfiles("test")
public class CaffeineCacheTest {

	// 테스트 캐시 이름 상수
	private static final String TEST_CACHE_1 = "test-cache-1";
	private static final String TEST_CACHE_2 = "test-cache-2";

	// 테스트 데이터 키 상수
	private static final int KEY_1 = 1;
	private static final int KEY_2 = 2;
	private static final int KEY_3 = 3;

	// 테스트 데이터 값 상수
	private static final String VALUE_1 = "Value 1";
	private static final String VALUE_2 = "Value 2";
	private static final String VALUE_3 = "Value3";
	private static final String ANOTHER_VALUE_1 = "Another Value 1";

	// 예상 결과 상수
	private static final int EXPECTED_CACHE_SIZE_ZERO = 0;
	private static final int EXPECTED_CACHE_SIZE_ONE = 1;
	private static final int EXPECTED_CACHE_SIZE_THREE = 3;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private CacheQueryService cacheQueryService;

	@BeforeEach
	void setUp() {
		cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

		// 테스트용 캐시 생성 및 데이터 추가
		Cache cache1 = cacheManager.getCache(TEST_CACHE_1);
		Cache cache2 = cacheManager.getCache(TEST_CACHE_2);

		if (cache1 != null) {
			cache1.put(KEY_1, VALUE_1);
			cache1.put(KEY_2, VALUE_2);
		}

		if (cache2 != null) {
			cache2.put(KEY_1, ANOTHER_VALUE_1);
		}

	}

	@Test
	void 특정_캐시에대한_저장_삭제에대한_작업이_원할하게_이뤄진다() {
		Cache cache = cacheManager.getCache(TEST_CACHE_1);
		cache.put(KEY_3, VALUE_3);

		Assertions.assertThat(EXPECTED_CACHE_SIZE_THREE).isEqualTo(cacheQueryService.getCacheKeys(TEST_CACHE_1).size());

		cacheQueryService.clearCache(cache.getName());
		Assertions.assertThat(EXPECTED_CACHE_SIZE_ZERO).isEqualTo(cacheQueryService.getCacheKeys(TEST_CACHE_1).size());
	}

	@Test
	void 해당_이름에대한_캐시가_호출되면_모든_항목을_비운다() {
		cacheQueryService.clearAllCache();
		Cache testCache = cacheManager.getCache(TEST_CACHE_1);
		Cache testCache2 = cacheManager.getCache(TEST_CACHE_2);

		int cacheSize = cacheQueryService.getCacheKeys(testCache.getName()).size();
		int cacheSize2 = cacheQueryService.getCacheKeys(testCache2.getName()).size();

		Assertions.assertThat(cacheSize).isEqualTo(EXPECTED_CACHE_SIZE_ZERO);
		Assertions.assertThat(cacheSize2).isEqualTo(EXPECTED_CACHE_SIZE_ZERO);
	}

	@Test
	void 특정_캐시의_키에대한_삭제_요청이_가면_정상적으로_비운다() {
		cacheQueryService.evictCache(TEST_CACHE_1, KEY_1);

		int cacheSize = cacheQueryService.getCacheKeys(TEST_CACHE_1).size();

		Assertions.assertThat(cacheSize).isEqualTo(EXPECTED_CACHE_SIZE_ONE);
	}

}
