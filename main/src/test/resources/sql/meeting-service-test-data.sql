INSERT INTO "user" (id, name, "orgId", activities, "profileImage", phone)
VALUES (1, '모임개설자', 1001,
        '[{"part": "서버", "generation": 33}, {"part": "iOS", "generation": 32}]',
        'profile1.jpg', '010-1234-5678'),
       (2, '승인신청자', 1002,
        '[{"part": "기획", "generation": 32}, {"part": "기획", "generation": 29}, {"part": "기획", "generation": 33}, {"part": "기획", "generation": 30}]',
        'profile2.jpg', '010-1111-2222'),
       (3, '승인신청자', 1003,
        '[{"part": "웹", "generation": 34}]',
        'profile3.jpg', '010-3333-4444'),
       (4, '대기신청자', 1004,
        '[{"part": "iOS", "generation": 32}, {"part": "안드로이드", "generation": 29}]',
        'profile4.jpg', '010-5555-5555'),
       (5, '모임개설자2', 1005,
        '[{"part": "iOS", "generation": 35}, {"part": "안드로이드", "generation": 34}]',
        'profile5.jpg', '010-6666-6666');

INSERT INTO meeting (id, "userId", title, category, "imageURL", "startDate", "endDate", capacity,
                     "desc", "processDesc", "mStartDate", "mEndDate", "leaderDesc",
                     note, "isMentorNeeded", "canJoinOnlyActiveGeneration", "createdGeneration",
                     "targetActiveGeneration", "joinableParts")
VALUES (1, 1, '스터디 구합니다1', '행사',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-24 00:00:00.000000', '2024-05-24 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', '스터디장 설명입니다.',
        '시간지키세요.', true, true, 35, 35, '{PM,SERVER}'),

       (2, 5, '스터디 구합니다 - 신청전', '스터디',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-25 00:00:00.000000', '2024-05-24 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', null,
        null, false, false, 34, null, '{PM,SERVER}'),

       (3, 5, '스터디 구합니다 - 신청후', '스터디',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-04-22 00:00:00.000000', '2024-04-22 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 23:59:59.000000', null,
        null, false, false, 34, null, '{PM,SERVER}');

INSERT INTO apply (type, "meetingId", "userId", "appliedDate", status)
VALUES (0, 1, 2, '2024-05-19 00:00:00.913489', 1),
       (0, 1, 3, '2024-05-19 00:00:02.413489', 1),
       (0, 1, 4, '2024-05-19 00:00:03.413489', 0);

