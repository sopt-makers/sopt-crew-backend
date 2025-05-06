package org.sopt.makers.crew.main.admin.v2.service;

import java.util.List;
import java.util.Map;

import org.sopt.makers.crew.main.entity.property.Property;
import org.sopt.makers.crew.main.entity.property.PropertyRepository;
import org.sopt.makers.crew.main.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {

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
			.orElseThrow(() -> new NotFoundException("프로퍼티 키가 존재하지 않습니다 해당 키 : " + propertyKey));
		propertyRepository.deleteByKey(propertyKey);
	}

}
