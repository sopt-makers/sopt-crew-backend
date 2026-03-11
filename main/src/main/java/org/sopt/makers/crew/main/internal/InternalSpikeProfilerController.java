package org.sopt.makers.crew.main.internal;

import org.sopt.makers.crew.main.internal.dto.SpikeProfilerResetResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerSnapshotResponseDto;
import org.sopt.makers.crew.main.internal.dto.SpikeProfilerTraceSnapshotResponseDto;
import org.sopt.makers.crew.main.meeting.v2.service.SpikeApplyProfiler;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@Profile({"local", "traffic"})
@RestController
@RequestMapping("/internal/meeting/stats/spike-profiler")
@RequiredArgsConstructor
public class InternalSpikeProfilerController {

	private final SpikeApplyProfiler spikeApplyProfiler;

	@GetMapping
	@Operation(summary = "[Internal] Spike apply profiler snapshot", description = "Spike apply 경로의 in-memory profiler snapshot을 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Spike profiler snapshot 조회 성공")
	})
	public ResponseEntity<SpikeProfilerSnapshotResponseDto> getSnapshot() {
		return ResponseEntity.ok(spikeApplyProfiler.snapshot());
	}

	@GetMapping("/traces")
	@Operation(summary = "[Internal] Spike apply profiler trace snapshot",
		description = "Spike apply 경로의 request-id(traceId) 기준 in-memory per-request trace snapshot을 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Spike profiler trace snapshot 조회 성공")
	})
	public ResponseEntity<SpikeProfilerTraceSnapshotResponseDto> getTraceSnapshot() {
		return ResponseEntity.ok(spikeApplyProfiler.traceSnapshot());
	}

	@PostMapping("/reset")
	@Operation(summary = "[Internal] Spike apply profiler reset", description = "Spike apply profiler 누적값을 초기화합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Spike profiler reset 성공")
	})
	public ResponseEntity<SpikeProfilerResetResponseDto> reset() {
		return ResponseEntity.ok(spikeApplyProfiler.reset());
	}
}
