-- 시퀀스 초기화
ALTER SEQUENCE "advertisement_id_seq" RESTART WITH 1;
ALTER SEQUENCE "apply_id_seq" RESTART WITH 1;
ALTER SEQUENCE "comment_id_seq" RESTART WITH 1;
ALTER SEQUENCE "like_id_seq" RESTART WITH 1;
ALTER SEQUENCE "tag_id_seq" RESTART WITH 1;
ALTER SEQUENCE "flash_id_seq" RESTART WITH 1;
ALTER SEQUENCE "post_id_seq" RESTART WITH 1;
ALTER SEQUENCE "meeting_id_seq" RESTART WITH 1;
ALTER SEQUENCE "notice_id_seq" RESTART WITH 1;
ALTER SEQUENCE "report_id_seq" RESTART WITH 1;
ALTER SEQUENCE "user_id_seq" RESTART WITH 1;
ALTER SEQUENCE "co_leader_id_seq" RESTART WITH 1;
ALTER SEQUENCE "subway_station_id_seq" RESTART WITH 1;
ALTER SEQUENCE "sopt_map_id_seq" RESTART WITH 1;
ALTER SEQUENCE "map_recommended_id_seq" RESTART WITH 1;

-- 테이블의 데이터 삭제
DELETE
FROM "map_recommended";
DELETE
FROM "sopt_map";
DELETE
FROM "subway_station";
DELETE
FROM "advertisement";
DELETE
FROM "apply";
DELETE
FROM "comment";
DELETE
FROM "like";
DELETE
FROM "tag";
DELETE
FROM "flash";
DELETE
FROM "post";
DELETE
FROM "meeting";
DELETE
FROM "notice";
DELETE
FROM "report";
DELETE
FROM "user";
DELETE
FROM "co_leader";
