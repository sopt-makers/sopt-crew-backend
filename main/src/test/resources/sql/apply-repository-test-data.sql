INSERT INTO "user" (id, name, activities, "profileImage", phone)
VALUES (1, '김삼순',
        '[{"part": "서버", "generation": 33}, {"part": "iOS", "generation": 32}]',
        'profile1.jpg', '010-1234-5678'),
       (2, '홍길동',
        '[{"part": "기획", "generation": 32}, {"part": "기획", "generation": 29}, {"part": "기획", "generation": 33}, {"part": "기획", "generation": 30}]',
        'profile2.jpg', '010-1111-2222'),
       (3, '김철수',
        '[{"part": "웹", "generation": 34}]',
        'profile3.jpg', '010-3333-4444'),
       (4, '이영지',
        '[{"part": "iOS", "generation": 32}, {"part": "안드로이드", "generation": 29}]',
        'profile4.jpg', '010-5555-5555');

INSERT INTO meeting (id, "userId", title, category, "imageURL", "startDate", "endDate", capacity,
                       "desc", "processDesc", "mStartDate", "mEndDate", "leaderDesc",
                       note, "isMentorNeeded", "canJoinOnlyActiveGeneration", "createdGeneration",
                       "targetActiveGeneration", "joinableParts")
VALUES (1, 1, '스터디 구합니다1', '행사',
        '[{"id": 0, "url": "https://makers-web-img.s3.ap-northeast-2.amazonaws.com/meeting/2024/05/19/79ba8312-0ebf-48a2-9a5e-b372fb8a9e64.png"}]',
        '2024-05-19 00:00:00.000000', '2024-05-24 23:59:59.000000', 10,
        '스터디 설명입니다.', '스터디 진행방식입니다.',
        '2024-05-29 00:00:00.000000', '2024-05-31 00:00:00.000000', '스터디장 설명입니다.',
        '시간지키세요.', true, false, 34, 34, '{PM,DESIGN,WEB,ANDROID,IOS,SERVER}');

INSERT INTO apply (type, "meetingId", "userId", content, "appliedDate", status)
VALUES (0, 1, 2, '전할말입니다1', '2024-05-19 00:00:00.913489', 1),
       (0, 1, 3, '전할말입니다2', '2024-05-19 00:00:02.413489', 1),
       (0, 1, 4, '전할말입니다3', '2024-05-19 00:00:03.413489', 0);