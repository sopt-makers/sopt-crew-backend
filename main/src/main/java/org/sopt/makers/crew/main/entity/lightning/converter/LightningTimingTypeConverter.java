package org.sopt.makers.crew.main.entity.lightning.converter;

import org.sopt.makers.crew.main.entity.lightning.enums.LightningTimingType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LightningTimingTypeConverter implements AttributeConverter<LightningTimingType, String> {

	@Override
	public String convertToDatabaseColumn(LightningTimingType lightningTimingType) {
		return lightningTimingType.getValue(); // LightningTimingType의 값을 반환
	}

	@Override
	public LightningTimingType convertToEntityAttribute(String dbData) {
		return LightningTimingType.ofValue(dbData); // dbData에 해당하는 LightningTimingType 객체를 반환
	}
}
