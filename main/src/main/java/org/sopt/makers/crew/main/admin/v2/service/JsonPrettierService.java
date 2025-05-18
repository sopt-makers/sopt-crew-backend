package org.sopt.makers.crew.main.admin.v2.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.service.vo.MainPageContentVo;
import org.sopt.makers.crew.main.admin.v2.service.vo.PropertyVo;

public interface JsonPrettierService {

	List<MainPageContentVo> prettierHomeContent(List<PropertyVo> homeProperties);

	Map<String, Object> readValue(String json) throws IOException;

	Map<String, String> prettierJson(List<PropertyVo> allProperties);
}
