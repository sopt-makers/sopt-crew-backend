package org.sopt.makers.crew.main.external.playground;


import org.sopt.makers.crew.main.external.playground.dto.response.PlaygroundUserResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "playgroundServer", url = "${playground.server.url}")
public interface PlaygroundServer {
	@GetMapping("${playground.server.endpoint}")
	PlaygroundUserResponseDto getUser(@RequestHeader("Authorization") String accessToken);

}
