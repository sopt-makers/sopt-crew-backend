package org.sopt.makers.crew.main.entity.apply.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ApplyTypeConverter implements AttributeConverter<EnApplyType, Integer> {
	@Override
	public Integer convertToDatabaseColumn(EnApplyType applyType) {
		return applyType.getValue();
	}

	@Override
	public EnApplyType convertToEntityAttribute(Integer dbData) {
		return EnApplyType.ofValue(dbData);
	}
}
