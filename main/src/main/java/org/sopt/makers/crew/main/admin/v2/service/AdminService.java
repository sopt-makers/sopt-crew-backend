package org.sopt.makers.crew.main.admin.v2.service;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.dto.HomeProperties;
import org.sopt.makers.crew.main.entity.property.Property;

public interface AdminService {

	Property findPropertyByKey(String key);

	List<Property> findAllProperties();

	void updateProperty(String key, Map<String, Object> properties);

	void addProperty(String key, Map<String, Object> properties);

	void deleteProperty(String propertyKey);

	HomeProperties findHomeProperties();
}
