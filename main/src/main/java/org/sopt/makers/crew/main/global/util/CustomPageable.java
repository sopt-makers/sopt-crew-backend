package org.sopt.makers.crew.main.global.util;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CustomPageable implements Pageable {

	private final int page;
	private final int size;
	private final Sort sort;

	public CustomPageable(int page, int take, Sort sort) {
		this.page = page;
		this.sort = sort;
		this.size = take;
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
		return (long)(page - 1) * size + size;
	}

	@Override
	public Sort getSort() {
		return this.sort;
	}

	@Override
	public Pageable next() {
		return new CustomPageable(this.page + 1, this.size, this.sort);
	}

	@Override
	public Pageable previousOrFirst() {
		return this.page == 0 ? this : new CustomPageable(this.page + 1, this.size, this.sort);
	}

	@Override
	public Pageable first() {
		return new CustomPageable(0, this.size, this.sort);
	}

	@Override
	public Pageable withPage(int pageNumber) {
		return new CustomPageable(pageNumber, this.size, this.sort);
	}

	@Override
	public boolean hasPrevious() {
		return this.page > 0;
	}
}
