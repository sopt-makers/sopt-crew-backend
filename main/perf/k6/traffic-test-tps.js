import http from "k6/http";
import { check } from "k6";

// ============================================
// 환경변수 기반 URL 구성
// ============================================
const BASE_URL = (__ENV.BASE_URL || "").replace(/\/$/, "");
const X_API_TEST = __ENV.X_API_TEST || "";
const X_USER_ID = __ENV.X_USER_ID || "";
const X_USER_IDS = (__ENV.X_USER_IDS || "")
    .split(",")
    .map((id) => id.trim())
    .filter(Boolean);
const AUTH_TOKEN = __ENV.AUTH_TOKEN || "";

const MEETING_ID = Number(__ENV.MEETING_ID || 720);
const CONTENT = __ENV.CONTENT || "꼭 지원하고 싶습니다.";

const TARGET_TPS = Number(__ENV.TARGET_TPS || 20);
const TEST_DURATION = __ENV.TEST_DURATION || "3m";
const PRE_ALLOCATED_VUS = Number(__ENV.PRE_ALLOCATED_VUS || 200);
const MAX_VUS = Number(__ENV.MAX_VUS || 400);
const MAX_LATENCY_MS = Number(__ENV.MAX_LATENCY_MS || 20000);

// ============================================
// 테스트 유저 선택
// ============================================
function getRequestUserId() {
    if (X_USER_IDS.length > 0) {
        return X_USER_IDS[(__ITER + __VU - 1) % X_USER_IDS.length];
    }

    return X_USER_ID;
}

// ============================================
// 테스트 설정
// ============================================
export const options = {
    scenarios: {
        apply_traffic: {
            executor: "constant-arrival-rate",
            rate: TARGET_TPS,
            timeUnit: "1s",
            duration: TEST_DURATION,
            preAllocatedVUs: PRE_ALLOCATED_VUS,
            maxVUs: MAX_VUS,
        },
    },
    thresholds: {
        http_req_duration: ["p(95)<20000"],
        http_req_failed: ["rate<0.05"],
    },
};

// ============================================
// 테스트 시작 시 설정 출력
// ============================================
export function setup() {
    if (!BASE_URL) {
        throw new Error("BASE_URL을 설정해주세요.");
    }

    if (!X_USER_ID && X_USER_IDS.length === 0) {
        throw new Error("X_USER_ID 또는 X_USER_IDS를 설정해주세요.");
    }

    console.log("========================================");
    console.log("Traffic TPS Test");
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log(`Target TPS: ${TARGET_TPS}`);
    console.log(`Duration: ${TEST_DURATION}`);
    console.log(`User Mode: ${X_USER_IDS.length > 0 ? `rotation(${X_USER_IDS.length})` : "single X_USER_ID"}`);
    console.log("========================================");

    return {
        applyUrl: `${BASE_URL}/meeting/v2/test/apply`,
    };
}

// ============================================
// 메인 테스트 로직
// ============================================
export default function (data) {
    const requestUserId = getRequestUserId();
    const body = JSON.stringify({
        meetingId: MEETING_ID,
        content: CONTENT,
    });

    const params = {
        headers: {
            "Content-Type": "application/json",
            Accept: "application/json",
            "X-API-TEST": X_API_TEST,
            "X-USER-ID": requestUserId,
            "Authorization": AUTH_TOKEN ? `Bearer ${AUTH_TOKEN}` : undefined,
        },
        timeout: "30s",
        tags: {
            name: "POST /meeting/v2/test/apply",
            type: "traffic",
        },
    };

    const res = http.post(data.applyUrl, body, params);

    check(res, {
        "apply success": (r) => r.status === 201,
        "latency OK": (r) => r.timings.duration < MAX_LATENCY_MS,
    });
}

// ============================================
// 테스트 종료 후 요약
// ============================================
export function teardown(data) {
    console.log("========================================");
    console.log("Traffic TPS test completed.");
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log("========================================");
}
