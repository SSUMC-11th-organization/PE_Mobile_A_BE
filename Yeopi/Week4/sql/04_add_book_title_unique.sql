-- [선택 3] 같은 제목의 도서 중복 등록 방지
-- 실행 전 중복 제목이 있는지 먼저 확인합니다. 결과가 있으면 중복 행을 정리한 뒤 ALTER를 실행합니다.
SELECT title, COUNT(*) AS cnt
FROM book
GROUP BY title
HAVING COUNT(*) > 1;

ALTER TABLE book
    ADD CONSTRAINT uk_book_title UNIQUE (title);
