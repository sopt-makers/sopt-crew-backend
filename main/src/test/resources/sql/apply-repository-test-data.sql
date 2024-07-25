CREATE TYPE meeting_joinableparts_enum AS ENUM ('PM', 'DESIGN', 'IOS', 'ANDROID', 'SERVER', 'WEB');

INSERT INTO "user" (id, name, "orgId", activities, "profileImage", phone)
VALUES (1, '김삼순', 1001,
        '[{"part": "서버", "generation": 33}, {"part": "iOS", "generation": 32}]',
        'profile1.jpg', '010-1234-5678'),
       (2, '홍길동', 1002,
        '[{"part": "기획", "generation": 32}, {"part": "기획", "generation": 29}, {"part": "기획", "generation": 33}, {"part": "기획", "generation": 30}]',
        'profile2.jpg', '010-1111-2222'),
       (3, '김철수', 1003,
        '[{"part": "웹", "generation": 34}]',
        'profile3.jpg', '010-3333-4444'),
       (4, '이영지', 1004,
        '[{"part": "iOS", "generation": 32}, {"part": "안드로이드", "generation": 29}]',
        'profile4.jpg', '010-5555-5555');

create table "meeting" (
                           "canJoinOnlyActiveGeneration" boolean not null,
                           "capacity" integer not null,
                           "createdGeneration" integer not null,
                           "id" serial not null,
                           "isMentorNeeded" boolean not null,
                           "targetActiveGeneration" integer,
                           "userId" integer,
                           "endDate" TIMESTAMP not null,
                           "mEndDate" TIMESTAMP not null,
                           "mStartDate" TIMESTAMP not null,
                           "startDate" TIMESTAMP not null,
                           "category" varchar(255) not null,
                           "desc" varchar(255) not null,
                           "leaderDesc" varchar(255) not null,
                           "note" varchar(255),
                           "processDesc" varchar(255) not null,
                           "targetDesc" varchar(255) not null,
                           "title" varchar(255) not null,
                           "imageURL" jsonb,
                           "joinableParts" meeting_joinableparts_enum[],
                           primary key ("id")
);

INSERT INTO meeting (id, "userId", title, category, "imageURL", "startDate", "endDate", capacity,
                       "desc", "processDesc", "mStartDate", "mEndDate", "leaderDesc", "targetDesc",
                       note, "isMentorNeeded", "canJoinOnlyActiveGeneration", "createdGeneration",
                       "targetActiveGeneration", "joinableParts")
VALUES (1, 1, '스터디 구합니다1', '행사',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-05-19 00:00:00.000000', '2024-05-24 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 00:00:00.000000', '스터디장 설명입니다.',
        '누구나 들어올 수 있어요.', '시간지키세요.', true, false, 34, 34, '{PM,DESIGN,WEB,ANDROID,IOS,SERVER}');

INSERT INTO apply (type, "meetingId", "userId", content, "appliedDate", status)
VALUES (0, 1, 2, '전할말입니다1', '2024-05-19 00:00:00.913489', 1),
       (0, 1, 3, '전할말입니다2', '2024-05-19 00:00:02.413489', 1),
       (0, 1, 4, '전할말입니다3', '2024-05-19 00:00:03.413489', 0);