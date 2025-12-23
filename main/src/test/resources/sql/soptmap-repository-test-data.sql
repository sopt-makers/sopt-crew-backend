-- SoptMap Repository Test Data
-- 테스트 시나리오: 페이지네이션, 필터링, 정렬, 추천 기능

-- User 데이터
INSERT INTO "user" (id, name, activities, "profileImage", phone)
VALUES (1, '테스터1', '[{"part":"서버","generation":34}]', 'profile1.jpg', '010-1111-1111'),
       (2, '테스터2', '[{"part":"iOS","generation":34}]', 'profile2.jpg', '010-2222-2222');

-- SubwayStation 데이터
INSERT INTO subway_station (id, name, lines, "createdTimestamp", "modifiedTimestamp")
VALUES (1, '강남역', '["LINE_2", "SINBUNDANG"]', '2025-12-22 17:00:00', '2025-12-22 17:00:00'),
       (2, '역삼역', '["LINE_2"]', '2025-12-22 17:00:00', '2025-12-22 17:00:00'),
       (3, '홍대입구역', '["LINE_2", "GYEONGUI_JUNGANG"]', '2025-12-22 17:00:00', '2025-12-22 17:00:00'),
       (4, '건대입구역', '["LINE_2", "LINE_7"]', '2025-12-22 17:00:00', '2025-12-22 17:00:00');

-- SoptMap 데이터 (25개 - 페이지네이션 테스트용)
-- FOOD 카테고리 (8개)
INSERT INTO sopt_map (id, place_name, description, map_tags, nearby_station_ids, creator_id, "createdTimestamp")
VALUES (1, '맛집 홍대점', '분위기 좋은 맛집', '["FOOD"]'::jsonb, '[3]'::jsonb, 1, '2024-12-22 17:00:00'),
       (2, '스시야 강남점', '신선한 스시', '["FOOD"]'::jsonb, '[1, 2]'::jsonb, 1, '2024-12-22 16:00:00'),
       (3, '파스타집', '이탈리안 레스토랑', '["FOOD"]'::jsonb, '[1]'::jsonb, 2, '2024-12-22 15:00:00'),
       (4, '한식당', '한국 전통 음식', '["FOOD"]'::jsonb, '[2]'::jsonb, 1, '2024-12-22 14:00:00'),
       (5, '중식당', '중국 요리', '["FOOD"]'::jsonb, '[3]'::jsonb, 2, '2024-12-22 13:00:00'),
       (6, '일식당', '일본 정통 요리', '["FOOD"]'::jsonb, '[1]'::jsonb, 1, '2024-12-22 12:00:00'),
       (7, '분식집', '떡볶이 맛집', '["FOOD"]'::jsonb, '[2]'::jsonb, 2, '2024-12-22 11:00:00'),
       (8, '치킨집', '후라이드가 맛있는', '["FOOD"]'::jsonb, '[3]'::jsonb, 1, '2024-12-22 10:00:00');

-- CAFE 카테고리 (10개)
INSERT INTO sopt_map (id, place_name, description, map_tags, nearby_station_ids, creator_id, "createdTimestamp")
VALUES (9, '카페 온더플랜', '분위기 좋은 카페', '["CAFE"]'::jsonb, '[1, 2]'::jsonb, 1, '2024-12-22 18:00:00'),
       (10, '스타벅스 역삼점', '체인 카페', '["CAFE"]'::jsonb, '[2]'::jsonb, 2, '2024-12-22 17:30:00'),
       (11, '투썸플레이스', '케이크 맛집', '["CAFE"]'::jsonb, '[1]'::jsonb, 1, '2024-12-22 09:00:00'),
       (12, '할리스커피', '조용한 카페', '["CAFE"]'::jsonb, '[3]'::jsonb, 2, '2024-12-22 08:00:00'),
       (13, '카페베네', '브런치 카페', '["CAFE"]'::jsonb, '[2]'::jsonb, 1, '2024-12-22 07:00:00'),
       (14, '엔제리너스', '커피 전문점', '["CAFE"]'::jsonb, '[1]'::jsonb, 2, '2024-12-22 06:00:00'),
       (15, '빽다방', '저렴한 카페', '["CAFE"]'::jsonb, '[3]'::jsonb, 1, '2024-12-22 05:00:00'),
       (16, '메가커피', '가성비 카페', '["CAFE"]'::jsonb, '[2]'::jsonb, 2, '2024-12-22 04:00:00'),
       (17, '컴포즈커피', '컵이 큰 카페', '["CAFE"]'::jsonb, '[1]'::jsonb, 1, '2024-12-22 03:00:00'),
       (18, '이디야커피', '동네 카페', '["CAFE"]'::jsonb, '[3]'::jsonb, 2, '2024-12-22 02:00:00');

-- ETC 카테고리 (5개)
INSERT INTO sopt_map (id, place_name, description, map_tags, nearby_station_ids, creator_id, "createdTimestamp")
VALUES (19, '헬스장', '운동하기 좋은 곳', '["ETC"]'::jsonb, '[1]'::jsonb, 1, '2024-12-22 01:00:00'),
       (20, 'PC방', '게임하기 좋은', '["ETC"]'::jsonb, '[2]'::jsonb, 2, '2024-12-21 23:00:00'),
       (21, '노래방', '스트레스 해소', '["ETC"]'::jsonb, '[3]'::jsonb, 1, '2024-12-21 22:00:00'),
       (22, '스터디카페', '조용한 공부방', '["ETC"]'::jsonb, '[1]'::jsonb, 2, '2024-12-21 21:00:00'),
       (23, '코인세탁소', '편리한 빨래방', '["ETC"]'::jsonb, '[2]'::jsonb, 1, '2024-12-21 20:00:00');

-- 복합 카테고리 (2개)
INSERT INTO sopt_map (id, place_name, description, map_tags, nearby_station_ids, creator_id, "createdTimestamp")
VALUES (24, '브런치카페', '브런치 맛집', '["FOOD", "CAFE"]'::jsonb, '[3]'::jsonb, 1, '2024-12-21 19:00:00'),
       (25, '베이커리카페', '빵 맛집', '["FOOD", "CAFE"]'::jsonb, '[1, 4]'::jsonb, 2, '2024-12-21 18:00:00');

-- MapRecommend 데이터 (추천 시나리오 테스트)
-- ID 1 (맛집 홍대점): 추천 15개
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (1, 1, 1, true),
       (2, 1, 2, true),
       (3, 1, 3, true),
       (4, 1, 4, true),
       (5, 1, 5, true),
       (6, 1, 6, true),
       (7, 1, 7, true),
       (8, 1, 8, true),
       (9, 1, 9, true),
       (10, 1, 10, true),
       (11, 1, 11, true),
       (12, 1, 12, true),
       (13, 1, 13, true),
       (14, 1, 14, true),
       (15, 1, 15, true);

-- ID 2 (스시야 강남점): 추천 12개 (userId=1이 추천함)
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (16, 2, 1, true), -- 현재 유저! (userId=1)
       (17, 2, 2, true),
       (18, 2, 3, true),
       (19, 2, 4, true),
       (20, 2, 5, true),
       (21, 2, 6, true),
       (22, 2, 7, true),
       (23, 2, 8, true),
       (24, 2, 9, true),
       (25, 2, 10, true),
       (26, 2, 11, true),
       (27, 2, 12, true);

-- ID 9 (카페 온더플랜): 추천 10개
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (28, 9, 2, true),
       (29, 9, 3, true),
       (30, 9, 4, true),
       (31, 9, 5, true),
       (32, 9, 6, true),
       (33, 9, 7, true),
       (34, 9, 8, true),
       (35, 9, 9, true),
       (36, 9, 10, true),
       (37, 9, 11, true);

-- ID 3 (파스타집): 추천 8개
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (38, 3, 2, true),
       (39, 3, 3, true),
       (40, 3, 4, true),
       (41, 3, 5, true),
       (42, 3, 6, true),
       (43, 3, 7, true),
       (44, 3, 8, true),
       (45, 3, 9, true);

-- ID 4 (한식당): 추천 5개
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (46, 4, 3, true),
       (47, 4, 4, true),
       (48, 4, 5, true),
       (49, 4, 6, true),
       (50, 4, 7, true);

-- ID 10 (스타벅스): 추천 3개
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (51, 10, 4, true),
       (52, 10, 5, true),
       (53, 10, 6, true);

-- ID 24 (브런치카페): 추천 2개
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (54, 24, 5, true),
       (55, 24, 6, true);

-- 비활성 추천 (테스트용 - 집계에서 제외되어야 함)
INSERT INTO map_recommended (id, sopt_map_id, user_id, active)
VALUES (56, 1, 100, false), -- active=false는 집계 안됨
       (57, 2, 101, false);

-- Sequence 재시작
ALTER SEQUENCE user_id_seq RESTART WITH 100;
ALTER SEQUENCE subway_station_id_seq RESTART WITH 100;
ALTER SEQUENCE sopt_map_id_seq RESTART WITH 100;
ALTER SEQUENCE map_recommended_id_seq RESTART WITH 100;
