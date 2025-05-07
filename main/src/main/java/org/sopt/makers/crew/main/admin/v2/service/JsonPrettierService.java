package org.sopt.makers.crew.main.admin.v2.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.service.vo.MainPageContentVo;
import org.sopt.makers.crew.main.entity.property.Property;

public interface JsonPrettierService {

	MainPageContentVo prettierHomeContent(Map<String, Object> json) throws IOException;

	List<MainPageContentVo> prettierHomeContent(List<Property> homeProperties);

	Map<String, Object> readValue(String json) throws IOException;

	Map<String, String> prettierJson(List<Property> allProperties);
}
