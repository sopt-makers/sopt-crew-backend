package org.sopt.makers.crew.main.entity.apply.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ApplyStatusConverter implements AttributeConverter<EnApplyStatus, Integer> {
	@Override
	public Integer convertToDatabaseColumn(EnApplyStatus applyStatus) {
		return applyStatus.getValue();
	}

	@Override
	public EnApplyStatus convertToEntityAttribute(Integer dbData) {
		return EnApplyStatus.ofValue(dbData);
	}
}