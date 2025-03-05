package org.sopt.makers.crew.main.global.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(name = "OrgIdListDto", description = "유저 orgId 리스트 Dto")
public class OrgIdListDto {
	private final List<Integer> orgIds;

	@JsonCreator
	public OrgIdListDto(
		@JsonProperty("orgIds") List<Integer> orgIds
	) {
		this.orgIds = orgIds;
	}

	public static OrgIdListDto of(List<Integer> orgIds) {
		return new OrgIdListDto(orgIds);
	}
}
