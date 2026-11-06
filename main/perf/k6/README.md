# 크루팀 k6 부하 테스트

## 스크립트 종류

| 스크립트 | 목적                      |
|---|-------------------------|
| `traffic-test-tps.js` | 모임 신청 API 기본 TPS로 고정 부하 테스트 |
| `spike-test-tps.js` | 모임 신청 API 스파이크 트래픽 테스트  |
| `spike-apply-with-read.js` | 신청 폭주 중 조회 API 지연 측정 (신청 진입 제한 도입 판단용) |
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

## 신청 폭주 중 조회 지연 테스트 (로컬)

`spike-apply-with-read.js`는 모집 오픈 순간 신청이 몰릴 때 **다른 API가 느려지는지**를 측정합니다.
신청 진입 제한(Semaphore, #910) 도입 여부를 판단하기 위한 테스트입니다. (#956)

### 시나리오

| 구간 | 시간(기본값) | 부하 | 보는 지표 |
|---|---|---|---|
| before | 0 ~ 20초 | 조회만 `READ_TPS` | `read_duration_before` (평소 조회) |
| spike | 20 ~ 30초 | 조회 + **신청 200건 동시 발사** | `read_duration_spike` ← **판단 기준** |
| after | 30 ~ 50초 | 조회만 | `read_duration_after` (회복 여부) |

- 조회는 인증 없이 DB를 조회하는 `GET /meeting/v2/org-user`를 사용합니다. `AUTH_TOKEN`을 주면 상세 조회도 함께 측정합니다.
- `METRICS_BASE_URL`을 주면 DB 커넥션 대기 수(`hikari_pending`), 사용 중 커넥션(`hikari_active`), CPU(`process_cpu_usage`)를 초당 `METRICS_POLL_RATE`회(기본 10회) 수집합니다. 신청 폭주는 1~2초 안에 끝나서 1초 간격으로는 순간값을 놓칩니다.
- 아래 판단 기준 중 하나라도 넘으면 해당 threshold가 실패로 표시됩니다.
  - `read_duration_spike`: spike 구간 조회 p95 < `READ_P95_LIMIT_MS` (기본 1000ms)
  - `read_failed{phase:spike}`: spike 구간 조회 실패율 < `READ_FAIL_RATE_LIMIT` (기본 0.01). 조회가 빠르게 실패하면 p95가 낮아져 통과하는 것을 막습니다.
  - `apply_duration`: 신청 최대 응답 시간 < `SPIKE_WINDOW`. 신청이 구간을 넘기면 그 뒤의 조회 지연이 `after`로 빠져 판단에서 누락되므로 실패로 처리합니다. 이때는 `SPIKE_WINDOW`를 늘려 다시 측정하세요.

### 1. 로컬 DB 준비

```bash
# 로컬에 이미 5432를 쓰는 DB가 있을 수 있어 5433을 사용합니다
docker run -d --name crew-loadtest-db -e POSTGRES_PASSWORD=postgres -p 5433:5432 postgres:15
sleep 5
docker exec crew-loadtest-db psql -U postgres -c 'create database crew' -c 'create database playground'

# ⚠️ schema.sql은 테이블을 drop 후 생성합니다. 반드시 로컬 DB에만 실행하세요.
docker exec -i crew-loadtest-db psql -U postgres -d crew < main/src/main/resources/schema.sql
docker exec -i crew-loadtest-db psql -U postgres -d crew < main/perf/k6/local/seed.sql
```

`seed.sql`은 모임장 1명(100000), 신청자 200명(100001~100200), 모임 1개(900001)를 만들고 이전 신청 기록을 지웁니다.
**측정할 때마다 다시 실행**해서 같은 조건에서 시작하세요.

### 2. 애플리케이션 실행

운영과 같은 조건(Tomcat 스레드 200, Hikari 커넥션 10, Caffeine, 콘솔 로그)인 `traffic` 프로필에 DB만 로컬로 바꿔 실행합니다.
JDK 17을 사용하고, 운영처럼 jar로 실행합니다.

```bash
cd main
./gradlew clean bootJar

java -Dspring.profiles.active=traffic -jar build/libs/app.jar \
  --spring.datasource.jdbc-url=jdbc:postgresql://localhost:5433/crew \
  --spring.datasource.username=postgres --spring.datasource.password=postgres \
  --spring.playground-datasource.jdbc-url=jdbc:postgresql://localhost:5433/playground \
  --spring.playground-datasource.username=postgres --spring.playground-datasource.password=postgres \
  --sentry.dsn= \
  > /tmp/crew-loadtest.log 2>&1
```

- `--sentry.dsn=`: 테스트 중 에러가 Sentry로 전송되지 않게 끕니다.
- 로그는 파일로 보냅니다. 터미널 출력이 느려서 결과가 왜곡되는 것을 막습니다.

### 3. 실행

```bash
k6 run \
  --env BASE_URL=http://localhost:4000 \
  --env X_API_TEST=<API_TEST_SECRET> \
  --env METRICS_BASE_URL=http://localhost:<ACTUATOR_PORT><ACTUATOR_PATH> \
  --env READ_TPS=20 \
  main/perf/k6/spike-apply-with-read.js
```

`API_TEST_SECRET`, `ACTUATOR_PORT`, `ACTUATOR_PATH`는 `application-secret.properties`의 값입니다.

| 변수 | 기본값 | 설명 |
|---|---|---|
| `APPLY_USERS` | 200 | 동시에 신청하는 사용자 수 |
| `USER_ID_START` | 100001 | 신청자 ID 시작값 (`X_USER_IDS`로 직접 지정 가능) |
| `MEETING_ID` | 900001 | 신청 대상 모임 |
| `READ_TPS` | 20 | 조회 초당 요청 수 |
| `READ_ORG_USER_ID` | 100000 | 조회 대상 사용자 |
| `WARMUP` / `SPIKE_WINDOW` / `COOLDOWN` | 20 / 10 / 20 | 구간 길이(초) |
| `READ_P95_LIMIT_MS` | 1000 | 판단 기준: spike 구간 조회 p95 |
| `READ_FAIL_RATE_LIMIT` | 0.01 | 판단 기준: spike 구간 조회 실패율 |
| `METRICS_POLL_RATE` | 10 | 액추에이터 지표 수집 횟수(초당) |

### 4. 판단 방법

1. **현재 코드**로 2~3회 측정하고 중앙값을 봅니다. `read_duration_spike` p95가 기준 이하면 진입 제한은 필요 없습니다.
2. 기준을 넘으면 **진입 제한을 붙인 코드**로 같은 조건에서 다시 측정해, `read_duration_spike`와 `hikari_pending`이 줄어드는지 확인합니다.

### 해석할 때 주의할 점

- **절대 수치보다 비교로 판단하세요.** 로컬 CPU는 운영 서버보다 빠를 가능성이 높고, DB가 같은 컴퓨터에 있어 쿼리 지연이 거의 없습니다. 운영에서는 커넥션을 더 오래 잡아 경합이 더 심할 수 있습니다.
- **`/test/apply`는 실제 신청보다 가볍습니다.** 신청 검증(신청 목록 전체 조회 등)이 없습니다. 로컬에서 기준에 아슬아슬하게 들어오면 운영에서는 넘을 수 있다고 보세요.
- k6, 애플리케이션, DB가 한 컴퓨터에서 CPU를 나눠 씁니다. k6 CPU 사용량이 높으면 결과가 흐려집니다.
- `hikari_pending`은 crew·playground 커넥션 풀의 합계입니다. 테스트 중 playground 풀은 거의 쓰이지 않습니다.

