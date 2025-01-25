package org.sopt.makers.crew.main.entity.flash.converter;

import org.sopt.makers.crew.main.entity.flash.enums.FlashTimingType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FlashTimingTypeConverter implements AttributeConverter<FlashTimingType, String> {

	@Override
	public String convertToDatabaseColumn(FlashTimingType flashTimingType) {
		return flashTimingType.getValue(); // FlashTimingType의 값을 반환
	}

	@Override
	public FlashTimingType convertToEntityAttribute(String dbData) {
		return FlashTimingType.ofValue(dbData); // dbData에 해당하는 FlashTimingType 객체를 반환
	}
}
