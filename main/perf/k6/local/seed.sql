-- ============================================
-- spike-apply-with-read.js 로컬 실행용 데이터
-- schema.sql을 먼저 적용한 crew DB에서 실행한다.
--   psql -h localhost -U postgres -d crew -f main/perf/k6/local/seed.sql
-- 여러 번 실행해도 되도록 작성했다. (신청 기록은 매번 비운다)
-- ============================================

-- schema.sql에는 테스트 신청 테이블이 없어서 만든다. (ApplyTest 엔티티 기준)
create table if not exists apply_test
(
    id                  serial primary key,
    type                integer   default 0 not null,
    "meetingId"         integer   not null references meeting on delete cascade,
    "userId"            integer   not null references "user" on delete cascade,
    content             varchar   not null,
    "appliedDate"       timestamp not null,
    status              integer   default 0 not null,
    "createdTimestamp"  timestamp default CURRENT_TIMESTAMP,
    "modifiedTimestamp" timestamp default CURRENT_TIMESTAMP
);

-- 모임장 1명 (조회 부하의 READ_ORG_USER_ID 기본값이기도 하다)
insert into "user" (id, name, activities, phone)
values (100000, 'loadtest-leader', '[{"part": "서버", "generation": 35}]'::jsonb, '010-0000-0000')
on conflict (id) do nothing;

-- 신청자 200명: 100001 ~ 100200 (k6 USER_ID_START, APPLY_USERS 기본값과 같다)
insert into "user" (id, name, activities, phone)
select g, 'loadtest-' || g, '[{"part": "웹", "generation": 35}]'::jsonb, '010-0000-0000'
from generate_series(100001, 100200) as g
on conflict (id) do nothing;

-- 신청 대상 모임 (k6 MEETING_ID 기본값 900001)
insert into meeting (id, "userId", title, category, "imageURL", "startDate", "endDate", capacity,
                     "desc", "processDesc", "mStartDate", "mEndDate", "isMentorNeeded",
                     "canJoinOnlyActiveGeneration", "joinableParts", "createdGeneration")
values (900001, 100000, '부하 테스트 모임', '행사',
        '[{"id": 0, "url": "https://example.com/loadtest.png"}]'::jsonb,
        now() - interval '1 day', now() + interval '30 day', 1000,
        '부하 테스트용 모임입니다.', '부하 테스트', now() + interval '31 day', now() + interval '60 day', false,
        false, array ['PM', 'DESIGN', 'IOS', 'ANDROID', 'SERVER', 'WEB']::meeting_joinableparts_enum[], 35)
on conflict (id) do nothing;

-- 반복 측정 시 이전 결과를 지운다
delete from apply_test where "meetingId" = 900001;
delete from apply where "meetingId" = 900001;

select (select count(*) from "user" where id between 100001 and 100200) as applicants,
       (select count(*) from meeting where id = 900001)                as meeting,
       (select count(*) from apply_test where "meetingId" = 900001)  as apply_test_rows;
