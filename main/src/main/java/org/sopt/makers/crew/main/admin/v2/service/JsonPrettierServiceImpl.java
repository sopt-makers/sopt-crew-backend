package org.sopt.makers.crew.main.admin.v2.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sopt.makers.crew.main.admin.v2.service.vo.MainPageContentVo;
import org.sopt.makers.crew.main.admin.v2.service.vo.PropertyVo;
import org.sopt.makers.crew.main.global.exception.ServerException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JsonPrettierServiceImpl implements JsonPrettierService {

	private final ObjectMapper objectMapper;

	@Override
	public List<MainPageContentVo> prettierHomeContent(List<PropertyVo> homeProperties) {
		return homeProperties.stream()
			.map(p -> {
				Map<String, Object> properties = p.properties();
				List<Integer> meetingIds = objectMapper.convertValue(properties.get("meetingIds"),
					new TypeReference<>() {
					});

				return MainPageContentVo.of(properties.get("title").toString(), meetingIds);
			}).toList();
	}

	@Override
	public Map<String, Object> readValue(String json) throws IOException {
		return objectMapper.readValue(json, new TypeReference<>() {
		});
	}

	@Override
	public Map<String, String> prettierJson(List<PropertyVo> allProperties) {
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		return allProperties.stream()
			.collect(Collectors.toMap(
				PropertyVo::key,
				p -> {
					try {
						return objectMapper.writeValueAsString(p.properties());
					} catch (JsonProcessingException e) {
						throw new ServerException(HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			));
	}

}
