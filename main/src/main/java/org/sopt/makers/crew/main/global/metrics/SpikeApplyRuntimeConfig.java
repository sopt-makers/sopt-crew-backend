package org.sopt.makers.crew.main.global.metrics;

import java.util.concurrent.Semaphore;

public final class SpikeApplyRuntimeConfig {
	public static final int SEMAPHORE_PERMITS = 30;
	public static final boolean USE_FAT_TX = true;
	public static final String TX_MODE_FAT = "fat";
	public static final String TX_MODE_SEQUENTIAL = "sequential";
	public static final String GATE_ON = "on";
	public static final String GATE_OFF = "off";

	private SpikeApplyRuntimeConfig() {
	}

	public static String currentTxMode() {
		return USE_FAT_TX ? TX_MODE_FAT : TX_MODE_SEQUENTIAL;
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
