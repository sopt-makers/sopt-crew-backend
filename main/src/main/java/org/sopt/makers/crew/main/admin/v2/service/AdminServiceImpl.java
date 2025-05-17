package org.sopt.makers.crew.main.admin.v2.service;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.admin.v2.dto.HomeProperties;
import org.sopt.makers.crew.main.admin.v2.service.vo.PropertyVo;
import org.sopt.makers.crew.main.entity.property.Property;
import org.sopt.makers.crew.main.entity.property.PropertyRepository;
import org.sopt.makers.crew.main.global.exception.ErrorStatus;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

	private static final String HOME_TOP_CONTENTS_KEY = "top";
	private static final String HOME_MIDDLE_CONTENTS_KEY = "middle";
	private static final String HOME_BOTTOM_CONTENTS_KEY = "bottom";
	private final PropertyRepository propertyRepository;

	@Override
	public Property findPropertyByKey(String key) {
		return propertyRepository.findByKey(key).orElseThrow(NotFoundException::new);
	}

	@Override
	public List<Property> findAllProperties() {
		return propertyRepository.findAll();
	}

	@Override
	@Transactional
	public void updateProperty(String key, Map<String, Object> properties) {
		Property property = propertyRepository.findByKey(key).orElseThrow(NotFoundException::new);

		property.updateProperties(properties);
	}

	@Override
	@Transactional
	public void addProperty(String key, Map<String, Object> properties) {
		propertyRepository.save(Property.builder().key(key).properties(properties).build());
	}

	@Override
	@Transactional
	public void deleteProperty(String propertyKey) {
		propertyRepository.findByKey(propertyKey)
			.orElseThrow(() -> new NotFoundException(ErrorStatus.NOT_FOUND_PROPERTY_KEY + propertyKey));
		propertyRepository.deleteByKey(propertyKey);
	}

	@Override
	public HomeProperties findHomeProperties() {
		return HomeProperties.from(List.of(
			PropertyVo.newInstance(findPropertyByKey(HOME_TOP_CONTENTS_KEY)),
			PropertyVo.newInstance(findPropertyByKey(HOME_MIDDLE_CONTENTS_KEY)),
			PropertyVo.newInstance(findPropertyByKey(HOME_BOTTOM_CONTENTS_KEY))
		));
	}

}
