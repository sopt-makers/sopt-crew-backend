package org.sopt.makers.crew.main.entity.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor(staticName = "of")
public class UserCreateDto {
	@Min(value = 0, message = "sopt org unique id 값은 number 타입이어야 합니다.")
	private final Long orgId;

	@NotBlank(message = "user name은 빈 문자열일 수 없습니다.")
	private final String name;

	private final String profileImage;
}
