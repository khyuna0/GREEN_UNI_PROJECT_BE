-- 우리 db에 맞게 필드 수정함 (2025.12.18) - 간략 버전

-- 대학
INSERT INTO college (id, name) VALUES (1, '공과대학');
INSERT INTO college (id, name) VALUES (2, '인문대학');
INSERT INTO college (id, name) VALUES (3, '사회과학대학');
INSERT INTO college (id, name) VALUES (4, '상경대학');


-- 학과
INSERT INTO department (id, name, college_id)
VALUES
    (101,'컴퓨터공학과', 1),
    (102,'전자공학과', 1),
    (103,'화학공학과', 1),
    (104,'기계공학과', 1),
    (105,'신소재공학과', 1);

INSERT INTO department (id, name, college_id)
VALUES
    (106, '철학과', 2),
    (107, '국사학과', 2),
    (108, '언어학과', 2),
    (109, '국어국문학과', 2),
    (110, '영어영문학과', 2);


-- 강의실
INSERT INTO room (id, college_id)
VALUES
    ('E601',1), ('E602',1), ('E701',1), ('E702',1), ('E801',1),
    ('E802',1), ('E901',1), ('E902',1), ('E904',1), ('E905',1);

INSERT INTO room (id, college_id)
VALUES
    ('H101', 2), ('H102', 2), ('H103', 2), ('H104', 2), ('H201', 2),
    ('H202', 2), ('H203', 2), ('H204', 2), ('H301', 2), ('H302', 2);

-- 교직원
INSERT INTO staff (id, name, birth_date, gender, address, tel, email, hire_date)
VALUES
    (230001,'박성희', '1995-09-03', '여성', '부산시 부산진구', '010-9930-2889', 'sungheepppp@gmail.com', '2023-01-01'),
    (230002,'이서영', '2000-01-05', '여성', '부산시 수영구', '010-0743-3282', 'os010312@naver.com', '2023-01-01'),
    (230003,'편용림','1992-07-07', '남성', '부산시 수영구 광안동', '010-2221-2221','yog4130@Gmail.com', '2023-01-01'),
    (230004, '김지현', '1990-07-26', '남성', '경남 양산시', '010-9019-0369', 'jhkim900726@gmail.com', '2024-01-01'),
    (230005, '황수정', '1991-12-26', '여성', '서울특별시 용산구', '010-1756-1574', 'hong1226@gmail.com', '2025-01-01');

INSERT INTO user (id, password, user_role)
VALUES
    (230001, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'staff'),
    (230002, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'staff'),
    (230003, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'staff'),
    (230004, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'staff'),
    (230005, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'staff');

-- 교수
INSERT INTO professor(id, name, birth_date, gender, address, tel, email, department_id, hire_date)
VALUES
    (23000001, '김근호', '1985-08-01', '남성', '부산시 부산진구', '010-5277-0535', 'tenco@green.com', 101, '2020-01-01'),
    (23000002, '이치승', '1960-11-04', '여성', '부산시 수영구', '010-5241-7184', 'lcs@green.com', 101, '2020-01-01'),
    (23000003, '김미정', '1971-10-19', '여성', '부산시 북구', '010-1162-9586', 'kmj@green.com', 102, '2020-01-01'),
    (23000004, '전대영', '1962-08-30', '남성', '부산시 동래구', '010-9938-8571', 'jdy@green.com', 102, '2020-01-01'),
    (23000005, '김효린', '1980-09-01', '여성', '부산시 해운대구', '010-8520-1748', 'khr@green.com', 103, '2020-01-01'),
    (23000006, '김현우', '1948-11-11', '여성', '부산시 해운대구', '010-1024-7785', 'khw@green.com', 103, '2020-01-01'),
    (23000007, '정다운', '1966-04-24', '여성', '부산시 강서구', '010-1642-9966', 'jdw@green.com', 104, '2023-01-01'),
    (23000008, '손주이', '1973-10-09', '여성', '부산시 수영구', '010-3425-8896', 'sjy@green.com', 104, '2023-01-01'),
    (23000009,'이현서', '1983-02-27', '남성', '부산시 영도구', '010-2025-5748', 'lhs@green.com', 105, '2023-01-01'),
    (23000010,'이지운', '1957-01-18', '여성', '부산시 사하구', '010-1566-9486', 'ljw@green.com', 105, '2023-01-01');


INSERT INTO professor(id, name, birth_date, gender, address, tel, email, department_id, hire_date)
VALUES
    (23000011, '구평회', '1985-03-12', '여성', '부산시 남구', '010-6356-2325', 'asdfqwe11@nate.com', 106, '2023-01-01'),
    (23000012, '김선우', '1991-12-14', '여성', '부산시 남구', '010-5234-1234', 'as5435@nate.com', 106, '2023-01-01'),
    (23000013, '이유신', '1990-05-14', '여성', '부산시 남구', '010-3423-3476', 'hfgdfg44@nate.com', 107, '2023-01-01'),
    (23000014, '고시근', '1981-08-21', '여성', '부산시 남구', '010-6765-6734', 'gvcfg4325@nate.com', 107, '2023-01-01'),
    (23000015, '김영진', '1976-06-03', '여성', '부산시 남구', '010-6345-3654', 'hgrds455@nate.com', 108, '2023-01-01'),
    (23000016, '이운식', '1979-05-04', '여성', '부산시 남구', '010-8642-9776', 'dfhyurewr444@nate.com', 108, (current_date)),
    (23000017, '김민수', '1978-11-07', '여성', '부산시 남구', '010-3456-7456', 'alstn134@nate.com', 109, (current_date)),
    (23000018, '이지환', '1987-10-25', '남성', '부산시 남구', '010-5423-6565', 'wlghks344@nate.com', 109, (current_date)),
    (23000019, '서원철', '1988-06-27', '남성', '부산시 남구', '010-8654-8644', 'dnjscjf345@nate.com', 110, (current_date)),
    (23000020, '이유신', '1991-08-30', '남성', '부산시 남구', '010-2345-4574', 'dsdfgert34@nate.com', 110, (current_date));

INSERT INTO user (id, password, user_role)
VALUES
    (23000001, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000002, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000003, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000004, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000005, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000006, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000007, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000008, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000009, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000010, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor');

INSERT INTO user (id, password, user_role)
VALUES
    (23000011, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000012, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000013, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000014, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000015, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000016, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000017, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000018, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000019, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor'),
    (23000020, '$2a$10$FhWCqhZC.zTPpVbRO6WeweMcDx5J56Y0nEB0btTRbon2.g4aY5lPu', 'professor');

-- 강의
INSERT INTO subject (id, name, professor_id, room_id, department_id, type, sub_year, semester, sub_day, start_time, end_time, credits, capacity, num_of_student)
VALUES
    (10001,'데이터통신', 23000001, 'E601', 101, '전공', 2023, 2, '월', 14, 17, 3, 20, 5),
    (10002,'딥러닝의 기초', 23000001, 'E601', 101, '전공', 2023, 2, '수', 9, 12, 3, 20, 3),
    (10003,'컴퓨터의 개념 및 실습', 23000002, 'E602', 101, '교양', 2023, 2, '화', 10, 12, 2, 30, 0),
    (10004,'컴퓨터 프로그래밍', 23000002, 'E602', 101, '전공', 2023, 2, '금', 15, 18, 3, 20, 0),
    (10005,'공학설계 입문', 23000003, 'E701', 102, '전공', 2023, 2, '목', 9, 12, 3, 20, 5),
    (10006,'반도체 공학', 23000003, 'E701', 102, '전공', 2023, 2, '목', 14, 17, 3, 20, 2),
    (10007,'융합전자연구', 23000004, 'E702', 102, '전공', 2023, 2, '금', 14, 17, 3, 20, 3),
    (10008,'기초 전기실험', 23000004, 'E702', 102, '전공', 2023, 2, '월', 14, 17, 3, 20, 0),
    (10009,'물리화학', 23000005, 'E801', 103, '전공', 2023, 2, '목', 12, 15, 3, 20, 0),
    (10010,'반응공학', 23000005, 'E801', 103, '전공', 2023, 1, '수', 12, 15, 3, 20, 4),
    (10011,'사고와 표현', 23000006, 'E802', 103, '교양', 2023, 1, '화', 11, 13, 2, 30, 3),
    (10012,'과학과 기술', 23000006, 'E802', 103, '교양', 2023, 1, '화', 13, 15, 2, 30, 5),
    (10013,'고체역학', 23000007, 'E901', 104, '전공', 2024, 1, '월', 13, 16, 3, 20, 7),
    (10014,'자유정의진리', 23000007, 'E901', 104, '교양', 2024, 1, '화', 9, 11, 2, 30, 4),
    (10015,'정보적 사고', 23000008, 'E902', 104, '교양', 2024, 1, '목', 9, 11, 2, 30, 4),
    (10016,'CAD기초', 23000008, 'E902', 104, '전공', 2024, 1, '화', 13, 15, 2, 20, 0),
    (10017,'에너지재료', 23000009, 'E904', 105, '전공', 2024, 1, '수', 11, 14, 3, 30,0),
    (10018,'나노재료합성', 23000009, 'E904', 105, '전공', 2024, 1, '목', 11, 14, 3, 30,0),
    (10019,'신소재공학개론', 23000010, 'E905', 105, '전공', 2024, 1, '월', 9, 12, 3, 30,0),
    (10020,'신소재기초실습', 23000010, 'E905', 105, '전공', 2024, 1, '월', 13, 16, 3, 30,0);

INSERT INTO subject (id, name, professor_id, room_id, department_id, type, sub_year, semester, sub_day, start_time, end_time, credits, capacity, num_of_student)
VALUES
    (10021, '불교철학사', 23000011, 'H101', 106, '전공', 2025, 1, '화', 09, 12, 3, 25, 3),
    (10022, '대륙합리론', 23000012, 'H102', 106, '전공', 2025, 1, '수', 10, 13, 3, 20, 17),
    (10023, '심리철학', 23000012, 'H102', 106, '교양', 2025, 1, '목', 14, 16, 2, 50, 25),
    (10024, '역사학개론', 23000013, 'H103', 107, '전공', 2025, 1, '월', 13, 16, 3, 25, 0),
    (10025, '동아시아사', 23000013, 'H103', 107, '전공', 2025, 1, '화', 11, 14, 3, 25, 0),
    (10026, '한국근대사', 23000014, 'H104', 107, '전공', 2025, 1, '금', 13, 16, 3, 20, 0),
    (10027, '한국사입문', 23000014, 'H104', 107, '교양', 2025, 1, '목', 10, 12, 2, 50, 0),
    (10028, '의미론', 23000015, 'H201', 108, '전공', 2025, 1, '월', 10, 13, 3, 25, 0),
    (10029, '형태론', 23000015, 'H201', 108, '전공', 2025, 1, '화', 14, 17, 3, 25, 0),
    (10030, '컴퓨터언어학', 23000016, 'H202', 108, '전공', 2025, 2, '수', 13, 16, 3, 20, 0),
    (10031, '이태리어', 23000016, 'H202', 108, '교양', 2025, 2, '금', 09, 11, 2, 50, 0),
    (10032, '고전문학연습', 23000017, 'H203', 109, '전공', 2025, 2, '월', 15, 18, 3, 25, 0),
    (10033, '국어정서법', 23000017, 'H203', 109, '전공', 2025, 2, '화', 12, 15, 3, 25, 0),
    (10034, '한국현대작가론', 23000018, 'H204', 109, '전공', 2025, 2, '수', 09, 12, 3, 20, 0),
    (10035, '국문학개론', 23000018, 'H204', 109, '교양', 2025, 2, '목', 16, 18, 2, 50, 0),
    (10036, '중세영문학', 23000019, 'H301', 110, '전공', 2025, 2, '금', 13, 16, 3, 25, 0),
    (10037, '영어발달사', 23000019, 'H301', 110, '전공', 2025, 2, '목', 10, 13, 3, 25, 0),
    (10038, '현대영국소설론', 23000020, 'H302', 110, '전공', 2025, 2, '수', 13, 16, 3, 20, 0),
    (10039, '영문학입문', 23000020, 'H302', 110, '교양', 2025, 2, '화', 15, 17, 2, 50, 0);
