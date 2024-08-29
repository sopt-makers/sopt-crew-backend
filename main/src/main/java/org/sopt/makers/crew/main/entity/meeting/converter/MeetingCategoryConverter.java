package org.sopt.makers.crew.main.entity.meeting.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import org.sopt.makers.crew.main.entity.meeting.enums.MeetingCategory;

@Converter
public class MeetingCategoryConverter implements AttributeConverter<MeetingCategory, String> {

	@Override
	public String convertToDatabaseColumn(MeetingCategory meetingCategory) {
		return meetingCategory.getValue();
	}

	@Override
	public MeetingCategory convertToEntityAttribute(String dbData) {
		return MeetingCategory.ofValue(dbData);
	}
}
