package org.sopt.makers.crew.main.dining.v2.service;

import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetAttendeesQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.query.DiningV2GetDiningsQueryDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateDiningBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.request.DiningV2CreateAndUpdateRoundBodyDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateDiningResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2CreateRoundResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendanceResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeeManagementResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetAttendeesResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetPayerRequestsResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningDetailResponseDto;
import org.sopt.makers.crew.main.dining.v2.dto.response.DiningV2GetDiningsResponseDto;

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

	void applyAttendance(Integer meetingId, Integer roundId, Integer userId);

	void cancelAttendance(Integer meetingId, Integer roundId, Integer userId);

	DiningV2GetAttendanceResponseDto getAttendance(Integer meetingId, Integer roundId, Integer userId);

	DiningV2GetAttendeeManagementResponseDto getAttendees(Integer meetingId, Integer roundId,
		DiningV2GetAttendeesQueryDto queryCommand, Integer userId);

	DiningV2GetAttendeesResponseDto getAttendanceAttendees(Integer meetingId, Integer roundId, Integer userId);

	void addAttendee(Integer meetingId, Integer roundId, Integer attendeeUserId, Integer userId);

	void deleteAttendee(Integer meetingId, Integer roundId, Integer attendeeUserId, Integer userId);

	void requestPayer(Integer meetingId, Integer roundId, Integer userId);

	DiningV2GetPayerRequestsResponseDto getPayerRequests(Integer meetingId, Integer userId);

	void approvePayer(Integer meetingId, Integer roundId, Integer requesterUserId, Integer userId);

	void rejectPayer(Integer meetingId, Integer roundId, Integer requesterUserId, Integer userId);

	void revokePayer(Integer meetingId, Integer roundId, Integer requesterUserId, Integer userId);

	DiningV2GetDiningsResponseDto getDinings(DiningV2GetDiningsQueryDto queryCommand, Integer userId);

	DiningV2GetDiningDetailResponseDto getDiningDetail(Integer meetingId, Integer userId);

	int closeExpiredRounds();
}
