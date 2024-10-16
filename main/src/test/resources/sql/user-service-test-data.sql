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
        'profile4.jpg', '010-5555-5555'),
       (5, '김솝트', 1005,
        '[{"part": "iOS", "generation": 32}, {"part": "안드로이드", "generation": 29}]',
        'profile4.jpg', '010-5555-5555');

INSERT INTO meeting ("userId", title, category, "imageURL", "startDate", "endDate", capacity,
                     "desc", "processDesc", "mStartDate", "mEndDate", "leaderDesc",
                     note, "isMentorNeeded", "canJoinOnlyActiveGeneration", "createdGeneration",
                     "targetActiveGeneration", "joinableParts")
VALUES (1, '스터디 구합니다1', '행사',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-24 00:00:00.000000', '2024-05-24 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', '스터디장 설명입니다.',
        '시간지키세요.', true, true, 35, 35, '{PM,SERVER}'),

       (4, '스터디 구합니다 - 신청전', '스터디',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-25 00:00:00.000000', '2024-05-24 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', null,
        null, false, false, 34, null, '{PM,SERVER}'),

       (4, '스터디 구합니다 - 신청후', '스터디',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-22 00:00:00.000000', '2024-04-22 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', null,
        null, false, false, 34, null, '{PM,SERVER}'),

       (4, '세미나 구합니다 - 신청후', '세미나',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-22 00:00:00.000000', '2024-04-22 23:59:59.000000', 13,
        '세미나 설명입니다.', '세미나 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', null,
        null, false, false, 34, null, '{WEB, IOS}');

INSERT INTO apply (type, "meetingId", "userId", "appliedDate", status)
VALUES (0, 1, 2, '2024-05-19 00:00:00.913489', 0),
       (0, 2, 2, '2024-05-19 00:00:02.413489', 0),
       (0, 3, 2, '2024-05-19 00:00:03.413489', 0),
       (0, 4, 2, '2024-05-19 00:00:03.413489', 1);

INSERT INTO co_leader ("meetingId", "userId")
VALUES (4, 3),
       (4, 5);