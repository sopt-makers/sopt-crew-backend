import http from "k6/http";
import { check } from "k6";

// ============================================
// 환경변수 기반 URL 구성 (실행 전 포트 확인)
// ============================================
const BASE_URL = (__ENV.BASE_URL || "http://localhost:4002").replace(/\/$/, "");
const X_API_TEST = __ENV.X_API_TEST || "";
const X_USER_ID = __ENV.X_USER_ID || "";
const X_USER_IDS = (__ENV.X_USER_IDS || "")
    .split(",")
    .map((id) => id.trim())
    .filter(Boolean);
const AUTH_TOKEN = __ENV.AUTH_TOKEN || "";

const MEETING_ID = Number(__ENV.MEETING_ID || 720);
const CONTENT = __ENV.CONTENT || "꼭 지원하고 싶습니다.";

const BASE_TPS = Number(__ENV.BASE_TPS || 5);
const SPIKE_TPS = Number(__ENV.SPIKE_TPS || 100); // 목표 TPS 100으로 설정

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
        apply_spike: {
            executor: "ramping-arrival-rate",
            startRate: BASE_TPS,
            timeUnit: "1s",
            // SSH 터널 안정성을 위해 최대 VU를 900으로 조절
            preAllocatedVUs: 400,
            maxVUs: 900,
            stages: [
                { target: BASE_TPS, duration: "20s" },
                { target: SPIKE_TPS, duration: "10s" },
                { target: SPIKE_TPS, duration: "1m" },
                { target: BASE_TPS, duration: "10s" },
                { target: 0, duration: "10s" },
            ],
        },
    },
    thresholds: {
        http_req_duration: ["p(99)<25000"], // 응답 지연을 고려해 25초로 완화 (병목 확인용)
        http_req_failed: ["rate<0.05"],    // 에러 5% 미만
    },
};

// ============================================
// 테스트 시작 시 확인할 로그
// ============================================
export function setup() {
    if (!X_USER_ID && X_USER_IDS.length === 0) {
        throw new Error("X_USER_ID 또는 X_USER_IDS를 설정해주세요.");
    }

    console.log("========================================");
    console.log("Spike TPS Test");
    console.log(`Target URL: ${BASE_URL}/meeting/v2/test/apply`);
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log(`Base TPS: ${BASE_TPS}`);
    console.log(`Spike TPS: ${SPIKE_TPS}`);
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

    // 여러 테스트 계정을 넣으면 요청마다 순환하면서 신청
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
            type: "spike",
        },
    };

    const res = http.post(data.applyUrl, body, params);

    check(res, {
        "apply success": (r) => r.status === 201,
        "latency OK": (r) => r.timings.duration < 20000,
    });
}

// ============================================
// 테스트 종료 후 요약
// ============================================
export function teardown(data) {
    console.log("========================================");
    console.log("Spike TPS test completed.");
    console.log(`Target: ${data.applyUrl}`);
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log("========================================");
}
