import http from "k6/http";
import exec from "k6/execution";
import { check } from "k6";
import { Counter, Trend } from "k6/metrics";

// ============================================
// 목적
// ============================================
// 모집 오픈 순간 신청이 한꺼번에 몰릴 때 "다른 API(조회)가 느려지는가"를 측정한다.
// 신청 진입 제한(Semaphore, #910) 도입 여부를 판단하기 위한 테스트다. (#956)
//
// 타임라인
//   0s ~ WARMUP            : 조회만 일정하게 → 평소 조회 응답 시간 (before)
//   WARMUP ~ +SPIKE_WINDOW : 신청 APPLY_USERS건을 동시에 발사 + 조회 계속 → 폭주 중 조회 응답 시간 (spike)
//   이후 ~ 끝               : 조회만 → 회복 여부 (after)
//
// 판단 기준: spike 구간 조회 p95가 READ_P95_LIMIT_MS 이하인지 (threshold로 PASS/FAIL 표시)

// ============================================
// 환경변수
// ============================================
const BASE_URL = (__ENV.BASE_URL || "http://localhost:4000").replace(/\/$/, "");
const X_API_TEST = __ENV.X_API_TEST || "";
const MEETING_ID = Number(__ENV.MEETING_ID || 900001);
const CONTENT = __ENV.CONTENT || "꼭 지원하고 싶습니다.";

// 신청자 ID: X_USER_IDS(CSV)가 있으면 사용, 없으면 USER_ID_START부터 APPLY_USERS명 (seed.sql 기본값과 동일)
const APPLY_USERS = Number(__ENV.APPLY_USERS || 200);
const USER_ID_START = Number(__ENV.USER_ID_START || 100001);
const X_USER_IDS = (__ENV.X_USER_IDS || "")
    .split(",")
    .map((id) => id.trim())
    .filter(Boolean);

// 조회 부하
const READ_TPS = Number(__ENV.READ_TPS || 20);
const READ_ORG_USER_ID = Number(__ENV.READ_ORG_USER_ID || 100000);
const AUTH_TOKEN = __ENV.AUTH_TOKEN || ""; // 있으면 상세 조회도 함께 측정 (토큰의 사용자가 로컬 DB에 있어야 함)

// 타이밍 (초)
const WARMUP = Number(__ENV.WARMUP || 20);
const SPIKE_WINDOW = Number(__ENV.SPIKE_WINDOW || 10);
const COOLDOWN = Number(__ENV.COOLDOWN || 20);
const TOTAL = WARMUP + SPIKE_WINDOW + COOLDOWN;

// 판단 기준
const READ_P95_LIMIT_MS = Number(__ENV.READ_P95_LIMIT_MS || 1000);

// 액추에이터 (선택): 예) http://localhost:<ACTUATOR_PORT><ACTUATOR_PATH>
const METRICS_BASE_URL = (__ENV.METRICS_BASE_URL || "").replace(/\/$/, "");
// 신청 폭주는 1~2초 안에 끝나므로 1초 간격으로는 순간값을 놓친다
const METRICS_POLL_RATE = Number(__ENV.METRICS_POLL_RATE || 10);

// ============================================
// 커스텀 지표
// ============================================
const readBefore = new Trend("read_duration_before", true);
const readSpike = new Trend("read_duration_spike", true);
const readAfter = new Trend("read_duration_after", true);
const applyDuration = new Trend("apply_duration", true);
const applyResult = new Counter("apply_result");
const hikariPending = new Trend("hikari_pending");
const hikariActive = new Trend("hikari_active");
const processCpu = new Trend("process_cpu_usage");

// ============================================
// 시나리오
// ============================================
const scenarios = {
    // 조회: 테스트 내내 일정한 TPS
    read_traffic: {
        executor: "constant-arrival-rate",
        exec: "readTraffic",
        rate: READ_TPS,
        timeUnit: "1s",
        duration: `${TOTAL}s`,
        preAllocatedVUs: Math.max(20, READ_TPS * 2),
        maxVUs: Math.max(100, READ_TPS * 10),
    },
    // 신청: WARMUP 시점에 APPLY_USERS명이 동시에 1번씩
    apply_spike: {
        executor: "per-vu-iterations",
        exec: "applySpike",
        vus: X_USER_IDS.length > 0 ? X_USER_IDS.length : APPLY_USERS,
        iterations: 1,
        startTime: `${WARMUP}s`,
        maxDuration: `${SPIKE_WINDOW + COOLDOWN}s`,
    },
};

if (METRICS_BASE_URL) {
    // DB 커넥션 풀 상태와 CPU를 초당 METRICS_POLL_RATE회 수집
    scenarios.metrics_poll = {
        executor: "constant-arrival-rate",
        exec: "pollMetrics",
        rate: METRICS_POLL_RATE,
        timeUnit: "1s",
        duration: `${TOTAL}s`,
        preAllocatedVUs: Math.max(4, METRICS_POLL_RATE),
        maxVUs: Math.max(8, METRICS_POLL_RATE * 3),
    };
}

export const options = {
    scenarios,
    thresholds: {
        // 판단 기준: 신청 폭주 중 조회 p95
        read_duration_spike: [`p(95)<${READ_P95_LIMIT_MS}`],
        // 참고용 (실패해도 판단 기준은 아님)
        read_duration_before: ["p(95)>=0"],
        read_duration_after: ["p(95)>=0"],
        apply_duration: ["p(95)>=0"],
        hikari_pending: ["max>=0"],
    },
    summaryTrendStats: ["avg", "med", "p(90)", "p(95)", "p(99)", "max"],
};

// ============================================
// 시작 시 확인
// ============================================
export function setup() {
    if (!X_API_TEST) {
        throw new Error("X_API_TEST(/meeting/v2/test/apply 시크릿)를 설정해주세요.");
    }

    console.log("========================================");
    console.log("Spike Apply With Read Test");
    console.log(`Base URL: ${BASE_URL}`);
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log(`Apply users: ${X_USER_IDS.length > 0 ? X_USER_IDS.length : APPLY_USERS} (동시 1회씩)`);
    console.log(`Read TPS: ${READ_TPS} (detail: ${AUTH_TOKEN ? "on" : "off"})`);
    console.log(`Timeline: warmup ${WARMUP}s → spike ${SPIKE_WINDOW}s → cooldown ${COOLDOWN}s`);
    console.log(`판단 기준: spike 구간 조회 p95 < ${READ_P95_LIMIT_MS}ms`);
    console.log(`Metrics: ${METRICS_BASE_URL || "off"}`);
    console.log("========================================");

    return { startedAt: Date.now() };
}

// ============================================
// 조회
// ============================================
function phaseOf(startedAt) {
    const elapsedSec = (Date.now() - startedAt) / 1000;
    if (elapsedSec < WARMUP) {
        return "before";
    }
    if (elapsedSec < WARMUP + SPIKE_WINDOW) {
        return "spike";
    }
    return "after";
}

function recordRead(phase, durationMs) {
    if (phase === "before") {
        readBefore.add(durationMs);
    } else if (phase === "spike") {
        readSpike.add(durationMs);
    } else {
        readAfter.add(durationMs);
    }
}

export function readTraffic(data) {
    const phase = phaseOf(data.startedAt);
    const params = {
        headers: { Accept: "application/json" },
        timeout: "30s",
        tags: { phase },
    };

    // 인증 없이 DB를 조회하는 API (사용자, 공동 모임장, 신청 내역 조회)
    const orgUserRes = http.get(
        `${BASE_URL}/meeting/v2/org-user?orgUserId=${READ_ORG_USER_ID}&page=1&take=12`,
        { ...params, tags: { ...params.tags, name: "GET /meeting/v2/org-user" } },
    );
    check(orgUserRes, { "org-user status is 200": (r) => r.status === 200 });
    recordRead(phase, orgUserRes.timings.duration);

    if (AUTH_TOKEN) {
        const detailRes = http.get(`${BASE_URL}/meeting/v2/${MEETING_ID}`, {
            ...params,
            headers: { ...params.headers, Authorization: `Bearer ${AUTH_TOKEN}` },
            tags: { ...params.tags, name: "GET /meeting/v2/{meetingId}" },
        });
        check(detailRes, { "detail status is 200": (r) => r.status === 200 });
        recordRead(phase, detailRes.timings.duration);
    }
}

// ============================================
// 신청
// ============================================
function applyUserId() {
    const index = exec.scenario.iterationInTest;
    if (X_USER_IDS.length > 0) {
        return X_USER_IDS[index % X_USER_IDS.length];
    }
    return String(USER_ID_START + index);
}

export function applySpike() {
    const res = http.post(
        `${BASE_URL}/meeting/v2/test/apply`,
        JSON.stringify({ meetingId: MEETING_ID, content: CONTENT }),
        {
            headers: {
                "Content-Type": "application/json",
                "X-API-TEST": X_API_TEST,
                "X-USER-ID": applyUserId(),
            },
            timeout: "30s",
            tags: { name: "POST /meeting/v2/test/apply" },
        },
    );

    applyDuration.add(res.timings.duration);
    applyResult.add(1, { status: String(res.status) });
    check(res, { "apply status is 201": (r) => r.status === 201 });
}

// ============================================
// 액추에이터 지표 수집
// ============================================
function metricValue(name) {
    const res = http.get(`${METRICS_BASE_URL}/metrics/${name}`, {
        timeout: "5s",
        tags: { name: "GET actuator metrics" },
    });
    if (res.status !== 200) {
        return null;
    }
    const measurement = (res.json("measurements") || []).find((m) => m.statistic === "VALUE");
    return measurement ? measurement.value : null;
}

export function pollMetrics() {
    const pending = metricValue("hikaricp.connections.pending");
    const active = metricValue("hikaricp.connections.active");
    const cpu = metricValue("process.cpu.usage");

    if (pending !== null) hikariPending.add(pending);
    if (active !== null) hikariActive.add(active);
    if (cpu !== null) processCpu.add(cpu * 100);
}
