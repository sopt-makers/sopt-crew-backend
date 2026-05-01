# 크루팀 k6 부하 테스트

## 스크립트 종류

| 스크립트 | 목적                      |
|---|-------------------------|
| `traffic-test-tps.js` | 모임 신청 API 기본 TPS로 고정 부하 테스트 |
| `spike-test-tps.js` | 모임 신청 API 스파이크 트래픽 테스트  |
| `load-test-meeting-read.js` | 모임 배너/목록/상세/추천 조회 테스트   |
| `load-test-community-read.js` | 게시글/댓글 조회 테스트           |
| `load-test-user.js` | 마이페이지 프로필/신청 모임/개설 모임 조회 테스트 |

## 공통 환경 변수

| 변수 | 설명 |
|---|---|
| `BASE_URL` | 대상 서버 URL |
| `MEETING_ID` | 테스트 대상 모임 ID |
| `POST_ID` | 커뮤니티 조회 테스트용 게시글 ID |
| `AUTH_TOKEN` | 조회 스크립트에서 사용할 Bearer 토큰 |
| `X_API_TEST` | `/meeting/v2/test/apply` 호출용 테스트 시크릿 |
| `X_USER_ID` | 신청 테스트용 유저 ID |
| `X_USER_IDS` | 신청 테스트용 유저 ID CSV. 예: `1001,1002,1003` |

## 사전 준비

```bash
# macOS
brew install k6

# 버전 확인
k6 version
```

## 실행 예시

### 1. 기본 TPS로 고정 부하 테스트

```bash
k6 run \
  --env BASE_URL=<BASE_URL> \
  --env MEETING_ID=720 \
  --env X_API_TEST=<X_API_TEST_SECRET> \
  --env X_USER_IDS=<TEST_USER_IDS_CSV> \
  --env TARGET_TPS=20 \
  main/perf/k6/traffic-test-tps.js
```

### 2. 모집 오픈 스파이크 테스트

```bash
k6 run \
  --env BASE_URL=<BASE_URL> \
  --env MEETING_ID=720 \
  --env X_API_TEST=<X_API_TEST_SECRET> \
  --env X_USER_IDS=<TEST_USER_IDS_CSV> \
  --env BASE_TPS=5 \
  --env SPIKE_TPS=100 \
  main/perf/k6/spike-test-tps.js
```

### 3. 모임 조회 테스트

```bash
k6 run \
  --env BASE_URL=<BASE_URL> \
  --env AUTH_TOKEN=<BEARER_TOKEN> \
  --env MEETING_ID=720 \
  --env TARGET_TPS=10 \
  main/perf/k6/load-test-meeting-read.js
```

### 4. 커뮤니티 조회 테스트

```bash
k6 run \
  --env BASE_URL=<BASE_URL> \
  --env AUTH_TOKEN=<BEARER_TOKEN> \
  --env MEETING_ID=720 \
  --env POST_ID=1234 \
  --env TARGET_TPS=8 \
  main/perf/k6/load-test-community-read.js
```

### 5. 유저 정보 조회 테스트

```bash
k6 run \
  --env BASE_URL=<BASE_URL> \
  --env AUTH_TOKEN=<BEARER_TOKEN> \
  --env TARGET_TPS=5 \
  main/perf/k6/load-test-user.js
```

## 스크립트별 메모

### `traffic-test-tps.js`

- `constant-arrival-rate` 기반으로 기본 TPS를 유지합니다.
- `MEETING_ID`, `X_API_TEST`, `X_USER_ID` 방식은 유지하되, 실제 TPS 테스트는 `X_USER_IDS` 순환 사용을 권장합니다.

### `spike-test-tps.js`

- 사용자가 몰리는 순간을 가정한 `ramping-arrival-rate` 시나리오입니다.
- 기본값은 `5 TPS -> 100 TPS -> 5 TPS` 입니다.
- 실제 spike 테스트는 `X_USER_IDS` 를 넣어 테스트 계정을 순환하는 방식을 추천합니다.

### `load-test-meeting-read.js`

- 한 iteration 에서 아래 4개를 함께 호출합니다.
  - `GET /meeting/v2/banner`
  - `GET /meeting/v2`
  - `GET /meeting/v2/{meetingId}`
  - `GET /meeting/v2/recommend`
- 읽기 흐름을 빠르게 확인하기 위한 가장 기본적인 조회 테스트입니다.

### `load-test-community-read.js`

- 한 iteration 에서 아래 4개를 함께 호출합니다.
  - `GET /post/v2`
  - `GET /post/v2/{postId}`
  - `GET /post/v2/count`
  - `GET /comment/v2`
- 게시글과 댓글 조회를 같이 확인하는 단순한 커뮤니티 읽기 테스트입니다.

### `load-test-user.js`

- 한 iteration 에서 아래 4개를 함께 호출합니다.
  - `GET /user/v2/profile/me`
  - `GET /user/v2/apply`
  - `GET /user/v2/meeting`
  - `GET /user/v2/interestedKeywords`
- 마이페이지 성격의 조회 API들을 한 번에 확인할 수 있습니다.

## 결과 확인

| 시점 | 방법 |
|---|---|
| 테스트 중 | `--out web-dashboard=open` 사용 후 브라우저 확인 |
| 테스트 후 | 터미널 요약 지표와 `check` 결과 확인 |

## 권장 사용 순서

1. 먼저 `load-test-meeting-read.js` 로 읽기 성능을 확인합니다.
2. 그 다음 `traffic-test-tps.js` 로 평시 신청 TPS를 확인합니다.
3. 마지막에 `spike-test-tps.js` 로 모집 순간 급증 패턴을 확인합니다.

추후 읽기 스크립트와 신청 스크립트를 분리하여 가독성과 유지보수성을 높일 예정입니다.
