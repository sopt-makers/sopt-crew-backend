package org.sopt.makers.crew.main.admin.v2.service.strategy;

public interface PropertyResponseStrategy<T> {
	T createResponse(String key);

	boolean supports(String key);
}
