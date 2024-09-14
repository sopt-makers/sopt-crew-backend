package org.sopt.makers.crew.main.external.playground;

import java.util.List;

import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlock;
import org.sopt.makers.crew.main.external.playground.entity.member_block.MemberBlockRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestService {
	private final MemberBlockRepository memberBlockRepository;

	@GetMapping
	public void test1() {
		// TODO: 해당 API는 차단 기능 구현할 때 참고용으로 만들었으며, 삭제가 필요하다.
		List<MemberBlock> all = memberBlockRepository.findAll();

		all.get(0);
	}
}
