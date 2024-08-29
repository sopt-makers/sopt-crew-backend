package org.sopt.makers.crew.main.internal.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PushNotificationResponseDto {

	@JsonProperty("status")
	private Integer status;

	@JsonProperty("success")
	private Boolean success;

	@JsonProperty("message")
	private String message;

}
