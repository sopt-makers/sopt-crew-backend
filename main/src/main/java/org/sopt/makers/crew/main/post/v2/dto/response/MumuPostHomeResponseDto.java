package org.sopt.makers.crew.main.post.v2.dto.response;

import java.util.List;

import org.sopt.makers.crew.main.entity.post.Post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "MumuPostHomeResponseDto", description = "무무 피드 홈에 노출될 정보를 담는 응답")
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MumuPostHomeResponseDto {

	@Schema(description = "참여중인 모임이 있는지 여부", example = "true")
	@NotNull
	private Boolean isEmptyAppliedMeeting;

	@Schema(description = "오늘 무무 피드를 작성했는지 여부", example = "true")
	@NotNull
	private Boolean hasWrittenTodayMumuPost;

	@Schema(description = "오늘의 무무 멘트", example = "true")
	@NotNull
	private String mumuText;

	@Schema(description = "피드 기본 정보")
	@NotNull
	private List<MumuPostHomeDto> mumuPostHomeDtos;

	public static MumuPostHomeResponseDto emptyAppliedMeeting(String mumuText) {
		return MumuPostHomeResponseDto.builder()
			.isEmptyAppliedMeeting(true)
			.hasWrittenTodayMumuPost(false)
			.mumuText(mumuText)
			.mumuPostHomeDtos(List.of())
			.build();
	}

	public static MumuPostHomeResponseDto notWrittenTodayMumuPost(List<Post> posts, String mumuText) {
		List<MumuPostHomeDto> mumuPostHomeDtos = convertMumuPostHomeDtos(posts);

		return MumuPostHomeResponseDto
			.builder()
			.isEmptyAppliedMeeting(false)
			.hasWrittenTodayMumuPost(false)
			.mumuText(mumuText)
			.mumuPostHomeDtos(mumuPostHomeDtos)
			.build();
	}

	public static MumuPostHomeResponseDto from(List<Post> posts, String mumuText) {
		List<MumuPostHomeDto> mumuPostHomeDtos = convertMumuPostHomeDtos(posts);

		return MumuPostHomeResponseDto.builder()
			.isEmptyAppliedMeeting(false)
			.hasWrittenTodayMumuPost(true)
			.mumuText(mumuText)
			.mumuPostHomeDtos(mumuPostHomeDtos)
			.build();
	}

	private static List<MumuPostHomeDto> convertMumuPostHomeDtos(List<Post> posts) {
		return posts.stream()
			.map(MumuPostHomeDto::from)
			.toList();
	}
}
