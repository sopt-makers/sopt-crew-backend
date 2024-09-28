package org.sopt.makers.crew.main.global.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CustomPageable implements Pageable {

	private final int page;
	private final int size;
	private final Sort sort;

	public CustomPageable(int page, Sort sort) {
		this.page = page;
		this.sort = sort;
		this.size = calculateSize(page);
	}

	private int calculateSize(int page) {
		// 첫 번째 페이지는 11개, 그 이후부터는 12개
		return page == 0 ? 11 : 12;
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
		return page == 0 ? 0 : 11 + (page - 1) * 12;
	}

	@Override
	public Sort getSort() {
		return this.sort;
	}

	@Override
	public Pageable next() {
		return new CustomPageable(this.page + 1, this.sort);
	}

	@Override
	public Pageable previousOrFirst() {
		return this.page == 0 ? this : new CustomPageable(this.page - 1, this.sort);
	}

	@Override
	public Pageable first() {
		return new CustomPageable(0, this.sort);
	}

	@Override
	public boolean hasPrevious() {
		return this.page > 0;
	}

	@Override
	public Pageable withPage(int pageNumber) {
		// 새로운 페이지 번호로 CustomPageable 생성
		return new CustomPageable(pageNumber, this.sort);
	}
}
