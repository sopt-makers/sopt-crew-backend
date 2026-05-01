package org.sopt.makers.crew.main.internal.dto;

public record UserOrgIdDto(
	Integer orgUserId
) {

	public static UserOrgIdDto from(Integer orgUserId) {
		return new UserOrgIdDto(orgUserId);
	}
}
