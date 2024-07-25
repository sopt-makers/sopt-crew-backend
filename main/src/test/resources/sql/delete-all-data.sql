-- 테이블의 데이터 삭제
DELETE FROM "apply";
DELETE FROM "comment";
DELETE FROM "like";
DELETE FROM "meeting";
DELETE FROM "notice";
DELETE FROM "post";
DELETE FROM "user";

-- 시퀀스 초기화
-- apply 테이블의 시퀀스 초기화
ALTER SEQUENCE "apply_id_seq" RESTART WITH 1;

-- comment 테이블의 시퀀스 초기화
ALTER SEQUENCE "comment_id_seq" RESTART WITH 1;

-- "like" 테이블의 시퀀스 초기화
ALTER SEQUENCE "like_id_seq" RESTART WITH 1;

-- meeting 테이블의 시퀀스 초기화
ALTER SEQUENCE "meeting_id_seq" RESTART WITH 1;

-- notice 테이블의 시퀀스 초기화
ALTER SEQUENCE "notice_id_seq" RESTART WITH 1;

-- post 테이블의 시퀀스 초기화
ALTER SEQUENCE "post_id_seq" RESTART WITH 1;

-- user 테이블의 시퀀스 초기화
ALTER SEQUENCE "user_id_seq" RESTART WITH 1;