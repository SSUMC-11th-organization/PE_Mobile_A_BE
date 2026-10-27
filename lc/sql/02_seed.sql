SET NAMES utf8mb4;

-- password는 6주차 bcrypt 전까지 더미 문자열로만 채웁니다. 실제 비밀번호를 넣지 마세요.
INSERT INTO member (email, password, nickname) VALUES
('noco@umc.com', '$2a$10$dummyEncodedPasswordValue', '수현'),
('minseo@umc.com', '$2a$10$dummyEncodedPasswordValue', '민서');

-- movie_id는 TMDB 영화 ID (550: 파이트 클럽, 155: 다크 나이트)
INSERT INTO rating (member_id, movie_id, score, comment) VALUES
(1, 550, 5, '최고의 영화!'),
(1, 155, 4, '다시 봐도 명작'),
(2, 550, 3, '호불호가 갈릴 것 같아요');
