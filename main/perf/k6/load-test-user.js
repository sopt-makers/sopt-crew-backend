import http from "k6/http";
import { check } from "k6";

// ============================================
// 환경변수 기반 URL 구성 (실행 전 포트 확인)
// ============================================
const BASE_URL = (__ENV.BASE_URL || "http://localhost:4002").replace(/\/$/, "");
const AUTH_TOKEN = __ENV.AUTH_TOKEN || "";

const TARGET_TPS = Number(__ENV.TARGET_TPS || 5);
const TEST_DURATION = __ENV.TEST_DURATION || "2m";

// ============================================
// 테스트 설정
// ============================================
export const options = {
    scenarios: {
        user_dashboard: {
            executor: "constant-arrival-rate",
            rate: TARGET_TPS,
            timeUnit: "1s",
            duration: TEST_DURATION,
            preAllocatedVUs: 30,
            maxVUs: 80,
        },
    },
    thresholds: {
        http_req_duration: ["p(95)<5000"],
        http_req_failed: ["rate<0.05"],
    },
};

// ============================================
// 테스트 시작 시 설정 출력
// ============================================
export function setup() {
    console.log("========================================");
    console.log("User Dashboard Test");
    console.log(`Target URL: ${BASE_URL}/user/v2`);
    console.log(`Target TPS: ${TARGET_TPS}`);
    console.log(`Duration: ${TEST_DURATION}`);
    console.log("========================================");

    return {
        profileUrl: `${BASE_URL}/user/v2/profile/me`,
        applyUrl: `${BASE_URL}/user/v2/apply`,
        meetingUrl: `${BASE_URL}/user/v2/meeting`,
        keywordsUrl: `${BASE_URL}/user/v2/interestedKeywords`,
    };
}

// ============================================
// 메인 테스트 로직
// ============================================
export default function (data) {
    const params = {
        headers: {
            Accept: "application/json",
            "Authorization": AUTH_TOKEN ? `Bearer ${AUTH_TOKEN}` : undefined,
        },
        timeout: "10s",
    };

    const responses = http.batch([
        ["GET", data.profileUrl, null, {...params, tags: {name: "GET /user/v2/profile/me"}}],
        ["GET", data.applyUrl, null, {...params, tags: {name: "GET /user/v2/apply"}}],
        ["GET", data.meetingUrl, null, {...params, tags: {name: "GET /user/v2/meeting"}}],
        ["GET", data.keywordsUrl, null, {...params, tags: {name: "GET /user/v2/interestedKeywords"}}],
    ]);

    check(responses[0], {
        "profile status is 200": (r) => r.status === 200,
    });

    check(responses[1], {
        "apply meeting status is 200": (r) => r.status === 200,
    });

    check(responses[2], {
        "created meeting status is 200": (r) => r.status === 200,
    });

    check(responses[3], {
        "interested keywords status is 200": (r) => r.status === 200,
    });
}

// ============================================
// 테스트 종료 후 요약
// ============================================
export function teardown(data) {
    console.log("========================================");
    console.log("User dashboard test completed.");
    console.log(`Profile URL: ${data.profileUrl}`);
    console.log(`Meeting URL: ${data.meetingUrl}`);
    console.log("========================================");
}
