package org.sopt.makers.crew.main.global.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * AdvertisementCustomPageable : 광고 관련된 컨텐츠에 대해 페이지네이션이 필요할 때 사용하는 클래스
 *
 * @implSpec : 광고 때문에 첫페이지는 11개를, 나머지는 12개를 반환해야 한다.
 * @implNote : 일반적인 페이지네이션 방식과 다르기에 해당 클래스를 따로 만들어 커스텀화하였다.
 * */
public class AdvertisementCustomPageable implements Pageable {

	private final int page;
	private final int size;
	private final Sort sort;

	public AdvertisementCustomPageable(int page, Sort sort) {
		this.page = page;
		this.sort = sort;
		this.size = calculateSize(page);
	}

	private int calculateSize(int page) {
		// 첫 번째 페이지는 11개, 그 이후부터는 12개
		if (page == 0) {
			return 11;
		}
		return 12;
	}

	@Override
	public int getPageNumber() {
		return this.page;
	}

	@Override
	public int getPageSize() {
		return this.size;
	}

	@Override
	public long getOffset() {
		// 오프셋 계산
		// 첫 번째 페이지는 11개, 그 이후부터는 12개 고려
		if (page == 0) {
			return 0;
		}
		return 11 + (page - 1) * 12L;
	}

	@Override
	public Sort getSort() {
		return this.sort;
	}

	@Override
	public Pageable next() {
		return new AdvertisementCustomPageable(this.page + 1, this.sort);
	}

	@Override
	public Pageable previousOrFirst() {
		if (page == 0) {
			return this;
		}
		return new AdvertisementCustomPageable(this.page - 1, this.sort);
	}

	@Override
	public Pageable first() {
		return new AdvertisementCustomPageable(0, this.sort);
	}

	@Override
	public boolean hasPrevious() {
		return this.page > 0;
	}

	@Override
	public Pageable withPage(int pageNumber) {
		// 새로운 페이지 번호로 CustomPageable 생성
		return new AdvertisementCustomPageable(pageNumber, this.sort);
	}
}
