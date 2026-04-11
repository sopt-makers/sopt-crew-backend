import http from "k6/http";
import { check } from "k6";

// ============================================
// 환경변수 기반 URL 구성
// ============================================
const BASE_URL = (__ENV.BASE_URL || "").replace(/\/$/, "");
const AUTH_TOKEN = __ENV.AUTH_TOKEN || "";

const MEETING_ID = Number(__ENV.MEETING_ID || 720);
const POST_ID = Number(__ENV.POST_ID || 1);
const PAGE = Number(__ENV.PAGE || 1);
const POST_TAKE = Number(__ENV.POST_TAKE || 10);
const COMMENT_TAKE = Number(__ENV.COMMENT_TAKE || 20);

const TARGET_TPS = Number(__ENV.TARGET_TPS || 8);
const TEST_DURATION = __ENV.TEST_DURATION || "2m";

// ============================================
// 테스트 설정
// ============================================
export const options = {
    scenarios: {
        community_read: {
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
    console.log("Community Read Test");
    console.log(`Meeting ID: ${MEETING_ID}`);
    console.log(`Post ID: ${POST_ID}`);
    console.log(`Target TPS: ${TARGET_TPS}`);
    console.log(`Duration: ${TEST_DURATION}`);
    console.log("========================================");

    return {
        postListUrl: `${BASE_URL}/post/v2?meetingId=${MEETING_ID}&page=${PAGE}&take=${POST_TAKE}`,
        postDetailUrl: `${BASE_URL}/post/v2/${POST_ID}`,
        postCountUrl: `${BASE_URL}/post/v2/count?meetingId=${MEETING_ID}`,
        commentUrl: `${BASE_URL}/comment/v2?postId=${POST_ID}&page=${PAGE}&take=${COMMENT_TAKE}`,
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
        ["GET", data.postListUrl, null, {...params, tags: {name: "GET /post/v2"}}],
        ["GET", data.postDetailUrl, null, {...params, tags: {name: "GET /post/v2/{postId}"}}],
        ["GET", data.postCountUrl, null, {...params, tags: {name: "GET /post/v2/count"}}],
        ["GET", data.commentUrl, null, {...params, tags: {name: "GET /comment/v2"}}],
    ]);

    check(responses[0], {
        "post list status is 200": (r) => r.status === 200,
    });

    check(responses[1], {
        "post detail status is 200": (r) => r.status === 200,
    });

    check(responses[2], {
        "post count status is 200": (r) => r.status === 200,
    });

    check(responses[3], {
        "comment status is 200": (r) => r.status === 200,
    });
}

// ============================================
// 테스트 종료 후 요약
// ============================================
export function teardown(data) {
    console.log("========================================");
    console.log("Community read test completed.");
    console.log("========================================");
}
