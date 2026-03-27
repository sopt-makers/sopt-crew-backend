package org.sopt.makers.crew.main.global.metrics;

import java.util.concurrent.Semaphore;

public final class SpikeApplyRuntimeConfig {
	public static final int SEMAPHORE_PERMITS = 20;
	public static final boolean USE_EVENT_NARROWED_WRITE_PATH = true;
	public static final boolean USE_EVENT_VALIDATION_BYPASS = true;
	public static final String TX_MODE_FAT = "fat";
	public static final String TX_MODE_SEQUENTIAL = "sequential";
	public static final String TX_MODE_EVENT_NARROWED = "event_narrowed";
	public static final String TX_MODE_EVENT_NARROWED_SYNTHETIC = "event_narrowed_synthetic";
	public static final String GATE_ON = "on";
	public static final String GATE_OFF = "off";

	private SpikeApplyRuntimeConfig() {
	}

	public static String currentTxMode() {
		if (USE_EVENT_NARROWED_WRITE_PATH && USE_EVENT_VALIDATION_BYPASS) {
			return TX_MODE_EVENT_NARROWED_SYNTHETIC;
		}
		return USE_EVENT_NARROWED_WRITE_PATH ? TX_MODE_EVENT_NARROWED : TX_MODE_SEQUENTIAL;
	}

	public static String currentGate() {
		return SEMAPHORE_PERMITS > 0 ? GATE_ON : GATE_OFF;
	}

	public static Semaphore createEventGate() {
		if (SEMAPHORE_PERMITS <= 0) {
			return null;
		}
		return new Semaphore(SEMAPHORE_PERMITS, true);
	}
}
