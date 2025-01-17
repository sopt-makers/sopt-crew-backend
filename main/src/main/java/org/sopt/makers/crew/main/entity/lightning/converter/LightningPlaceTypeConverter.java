package org.sopt.makers.crew.main.entity.lightning.converter;

import org.sopt.makers.crew.main.entity.lightning.enums.LightningPlaceType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LightningPlaceTypeConverter implements AttributeConverter<LightningPlaceType, String> {

	@Override
	public String convertToDatabaseColumn(LightningPlaceType lightningPlaceType) {
		return lightningPlaceType.getValue(); // LightningPlaceType의 값을 반환
	}

	@Override
	public LightningPlaceType convertToEntityAttribute(String dbData) {
		return LightningPlaceType.ofValue(dbData); // dbData에 해당하는 LightningPlaceType 객체를 반환
	}
}
