package org.sopt.makers.crew.main.dining.v2.service;

import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;

public interface DiningV2Service {

	DiningV2CreateDiningResponseDto createDining(DiningV2CreateAndUpdateDiningBodyDto requestBody, Integer userId);

	void updateDining(Integer meetingId, DiningV2CreateAndUpdateDiningBodyDto requestBody, Integer userId);

	void deleteDining(Integer meetingId, Integer userId);

	void endDining(Integer meetingId, Integer userId);

	DiningV2CreateRoundResponseDto createRound(Integer meetingId, DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Integer userId);

	void updateRound(Integer meetingId, Integer roundId, DiningV2CreateAndUpdateRoundBodyDto requestBody,
		Integer userId);

	void deleteRound(Integer meetingId, Integer roundId, Integer userId);

	void closeRound(Integer meetingId, Integer roundId, Integer userId);
}
