package org.sopt.makers.crew.main.entity.flash.converter;

import org.sopt.makers.crew.main.entity.flash.enums.FlashPlaceType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class FlashPlaceTypeConverter implements AttributeConverter<FlashPlaceType, String> {

	@Override
	public String convertToDatabaseColumn(FlashPlaceType flashPlaceType) {
		return flashPlaceType.getValue(); // FlashPlaceType의 값을 반환
	}

	@Override
	public FlashPlaceType convertToEntityAttribute(String dbData) {
		return FlashPlaceType.ofValue(dbData); // dbData에 해당하는 FlashPlaceType 객체를 반환
	}
}
