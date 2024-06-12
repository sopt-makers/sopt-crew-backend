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

INSERT INTO meeting (id, "userId", title, category, "imageURL", "startDate", "endDate", capacity,
                       "desc", "processDesc", "mStartDate", "mEndDate", "leaderDesc", "targetDesc",
                       note, "isMentorNeeded", "canJoinOnlyActiveGeneration", "createdGeneration",
                       "targetActiveGeneration", "joinableParts")
VALUES (2, 1, '스터디 구합니다2', '스터디',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-05-20 00:00:00.000000', '2024-05-25 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-30 00:00:00.000000', '2024-06-01 00:00:00.000000', '스터디장 설명입니다.',
        '누구나 들어올 수 있어요.', '시간지키세요.', true, false, 34, 34, '{PM,DESIGN,WEB,ANDROID,IOS,SERVER}');

INSERT INTO post (id, title, contents, "createdDate", "updatedDate", "viewCount", images, "userId", "meetingId", "commentCount", "likeCount")
VALUES
    (1, '제목1', '내용1', '2024-06-11 10:00:00', '2024-06-11 10:00:000000', 1, NULL, 1, 1, 0, 0),
    (2, '제목2', '내용2', '2024-06-11 10:00:01', '2024-06-11 10:00:000001', 2, NULL, 2, 1, 0, 0),
    (3, '제목3', '내용3', '2024-06-11 10:00:02', '2024-06-11 10:00:000002', 3, NULL, 3, 1, 0, 0),
    (4, '제목4', '내용4', '2024-06-11 10:00:03', '2024-06-11 10:00:000003', 4, NULL, 2, 2, 0, 0),
    (5, '제목5', '내용5', '2024-06-11 10:00:04', '2024-06-11 10:00:000004', 5, NULL, 4, 2, 0, 0);

INSERT INTO comment (id, contents, depth, "order", "createdDate", "updatedDate", "userId", "postId", "likeCount", "parentId")
VALUES
    (1, '첫 번째 댓글입니다.', 0, 0, '2024-06-12 10:00:00', '2024-06-12 10:00:000000', 1, 1, 0, NULL),
    (2, '두 번째 댓글입니다.', 0, 0, '2024-06-12 10:00:01', '2024-06-12 10:00:000001', 2, 1, 0, NULL),
    (3, '세 번째 댓글입니다.', 0, 0, '2024-06-12 10:00:02', '2024-06-12 10:00:000002', 2, 1, 0, NULL),
    (4, '네 번째 댓글입니다.', 0, 0, '2024-06-12 10:00:03', '2024-06-12 10:00:000003', 3, 1, 0, NULL),
    (5, '다섯 번째 댓글입니다.', 0, 0, '2024-06-12 10:00:04', '2024-06-12 10:00:000004', 4, 1, 0, NULL);

INSERT INTO "like" (id, "createdDate", "userId", "postId")
VALUES
    (1, '2024-06-12 11:00:00', 1, 1),
    (2, '2024-06-12 11:00:01', 2, 1);

UPDATE post SET "commentCount"=5, "likeCount"=2 WHERE id=1;
