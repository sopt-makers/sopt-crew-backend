import http from "k6/http";
import { check } from "k6";

// ============================================
// 환경변수 기반 URL 구성
// ============================================
const BASE_URL = (__ENV.BASE_URL || "").replace(/\/$/, "");
const AUTH_TOKEN = __ENV.AUTH_TOKEN || "";

const MEETING_ID = Number(__ENV.MEETING_ID || 720);
const PAGE = Number(__ENV.PAGE || 1);
const TAKE = Number(__ENV.TAKE || 12);
const IS_ONLY_ACTIVE_GENERATION = __ENV.IS_ONLY_ACTIVE_GENERATION || "true";

const TARGET_TPS = Number(__ENV.TARGET_TPS || 10);
const TEST_DURATION = __ENV.TEST_DURATION || "2m";

// ============================================
// 테스트 설정
// ============================================
export const options = {
    scenarios: {
        meeting_read: {
            executor: "constant-arrival-rate",
            rate: TARGET_TPS,
            timeUnit: "1s",
            duration: TEST_DURATION,
            preAllocatedVUs: 50,
            maxVUs: 100,
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
    if (!BASE_URL) {
        throw new Error("BASE_URL을 설정해주세요.");
    }

    console.log("========================================");
    console.log("Meeting Read Test");
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log(`Target TPS: ${TARGET_TPS}`);
    console.log(`Duration: ${TEST_DURATION}`);
    console.log("========================================");

    return {
        bannerUrl: `${BASE_URL}/meeting/v2/banner`,
        listUrl: `${BASE_URL}/meeting/v2?page=${PAGE}&take=${TAKE}&isOnlyActiveGeneration=${IS_ONLY_ACTIVE_GENERATION}`,
        detailUrl: `${BASE_URL}/meeting/v2/${MEETING_ID}`,
        recommendUrl: `${BASE_URL}/meeting/v2/recommend?meetingIds=${MEETING_ID}`,
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
        ["GET", data.bannerUrl, null, {...params, tags: {name: "GET /meeting/v2/banner"}}],
        ["GET", data.listUrl, null, {...params, tags: {name: "GET /meeting/v2"}}],
        ["GET", data.detailUrl, null, {...params, tags: {name: "GET /meeting/v2/{meetingId}"}}],
        ["GET", data.recommendUrl, null, {...params, tags: {name: "GET /meeting/v2/recommend"}}],
    ]);

    check(responses[0], {
        "banner status is 200": (r) => r.status === 200,
    });

    check(responses[1], {
        "meeting list status is 200": (r) => r.status === 200,
    });

    check(responses[2], {
        "meeting detail status is 200": (r) => r.status === 200,
    });

    check(responses[3], {
        "meeting recommend status is 200": (r) => r.status === 200,
    });
}

// ============================================
// 테스트 종료 후 요약
// ============================================
export function teardown(data) {
    console.log("========================================");
    console.log("Meeting read test completed.");
    console.log("========================================");
}
