package com.green.university.handler;

import com.green.university.dto.ChatLinkDto;
import com.green.university.intent.ChatIntent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PortalCatalog {

    // topic = 사용자 질문(intent)에대해
    // 안내문구 , reference, 바로가기링크, 키워드 목록
    public record Topic(
            ChatIntent intent,
            String title,
            List<String> references,
            List<ChatLinkDto> links,
            List<String> keywords
    ) {}

    // 한 번만 만들어서 재사용(캐싱)
    private final Map<ChatIntent, Topic> topics;

    public PortalCatalog() {
        this.topics = buildTopics();
    }

    public Map<ChatIntent, Topic> topics() {
        return topics;
    }

    /**
     * 라우터가 키워드 탐색할 때 사용할 “Topic 리스트”
     */
    public List<Topic> topicList() {
        return new ArrayList<>(topics.values());
    }

    /**
     * Mistral에게 보여줄 intent 후보 설명 문자열 생성
     * (너무 길면 분류가 흔들릴 수 있어서 title 정도만 간단히)
     */
    public String intentDescriptions() {
        StringBuilder sb = new StringBuilder();
        for (Topic t : topicList()) {
            sb.append("- ")
                    .append(t.intent().name())
                    .append(" (")
                    .append(t.title())
                    .append(")\n");
        }
        sb.append("- OUT_OF_SCOPE\n");
        return sb.toString();
    }

    // =========================================================
    // 내부: 토픽 빌드
    // =========================================================
    private Map<ChatIntent, Topic> buildTopics() {

        List<Topic> list = List.of(
                // ===== 성적 =====
                new Topic(
                        ChatIntent.GRADE_CURRENT,
                        "금학기 성적 조회 안내",
                        List.of("포털 > 성적 > 금학기 성적 조회"),
                        List.of(new ChatLinkDto("금학기 성적조회 바로가기", "/grade/current")),
                        List.of("금학기", "이번학기성적", "이번 학기 성적", "성적확인", "성적조회", "current grade")
                ),
                new Topic(
                        ChatIntent.GRADE_SEMESTER,
                        "학기별 성적 조회 안내",
                        List.of("포털 > 성적 > 학기별 성적 조회"),
                        List.of(new ChatLinkDto("학기별 성적조회 바로가기", "/grade/semester")),
                        List.of("학기별성적", "학기 성적", "지난학기성적", "semester grade")
                ),
                new Topic(
                        ChatIntent.GRADE_TOTAL,
                        "누계 성적 조회 안내",
                        List.of("포털 > 성적 > 누계 성적"),
                        List.of(new ChatLinkDto("누계 성적 바로가기", "/grade/total")),
                        List.of("누계", "전체성적", "총성적", "평점", "gpa", "total grade")
                ),

                // ===== 공지 / 학사일정 =====
                new Topic(
                        ChatIntent.NOTICE_LIST,
                        "공지사항 안내",
                        List.of("포털 > 학사 정보 > 공지사항"),
                        List.of(new ChatLinkDto("공지사항 바로가기", "/notice")),
                        List.of("공지", "공지사항", "notice")
                ),
                new Topic(
                        ChatIntent.SCHEDULE_LIST,
                        "학사일정 안내",
                        List.of("포털 > 학사 정보 > 학사일정"),
                        List.of(new ChatLinkDto("학사일정 바로가기", "/schedule")),
                        List.of("학사일정", "학사 일정", "일정", "스케줄", "schedule", "calendar")
                ),

                // ===== 수업/수강신청 =====
                new Topic(
                        ChatIntent.SUBJECT_LIST,
                        "전체 강의 조회 안내",
                        List.of("포털 > 수업 > 전체 강의 조회"),
                        List.of(new ChatLinkDto("전체 강의 조회 바로가기", "/subject/list")),
                        List.of("전체강의", "강의조회", "과목조회", "수업조회", "subject list")
                ),
                new Topic(
                        ChatIntent.SUGANG_LIST,
                        "강의 시간표 조회 안내",
                        List.of("포털 > 수강신청 > 강의 시간표 조회"),
                        List.of(new ChatLinkDto("강의 시간표 조회 바로가기", "/sugang/list")),
                        List.of("강의시간표", "시간표조회", "강의 시간표", "timetable", "시간표")
                ),
                new Topic(
                        ChatIntent.SUGANG_PRE,
                        "예비 수강 신청 안내",
                        List.of("포털 > 수강신청 > 예비 수강 신청"),
                        List.of(new ChatLinkDto("예비 수강 신청 바로가기", "/sugang/pre")),
                        List.of("예비수강", "예비 수강", "pre sugang")
                ),
                new Topic(
                        ChatIntent.SUGANG_APPLY,
                        "수강 신청 안내",
                        List.of("포털 > 수강신청 > 수강 신청"),
                        List.of(new ChatLinkDto("수강 신청 바로가기", "/sugang")),
                        List.of("수강신청", "수강 신청", "신청", "enroll")
                ),
                new Topic(
                        ChatIntent.SUGANG_TIMETABLE,
                        "수강 신청 내역 조회 안내",
                        List.of("포털 > 수강신청 > 수강 신청 내역 조회"),
                        List.of(new ChatLinkDto("수강 신청 내역 바로가기", "/sugang/timetable")),
                        List.of("수강내역", "수강 신청 내역", "신청내역", "내 시간표", "최종시간표")
                ),
                new Topic(
                        ChatIntent.SUGANG_PERIOD,
                        "수강신청 기간 설정 안내(교직원)",
                        List.of("포털 > 학사 관리 > 수강신청 기간 설정"),
                        List.of(new ChatLinkDto("수강신청 기간 설정 바로가기", "/sugang/period")),
                        List.of("기간설정", "수강신청기간", "수강신청 기간 설정")
                ),

                // ===== 사용자 =====
                new Topic(
                        ChatIntent.USER_INFO,
                        "내 정보 조회 안내",
                        List.of("포털 > MY > 내 정보 조회"),
                        List.of(new ChatLinkDto("내 정보 조회 바로가기", "/user/info")),
                        List.of("내정보", "내 정보", "회원정보", "user info", "프로필")
                ),
                new Topic(
                        ChatIntent.USER_PW,
                        "비밀번호 변경 안내",
                        List.of("포털 > MY > 비밀번호 변경"),
                        List.of(new ChatLinkDto("비밀번호 변경 바로가기", "/user/update/password")),
                        List.of("비밀번호", "비번", "비밀번호변경", "password")
                ),

                // ===== 휴학 =====
                new Topic(
                        ChatIntent.BREAK_APP,
                        "휴학 신청 안내",
                        List.of("포털 > MY > 휴학 신청"),
                        List.of(new ChatLinkDto("휴학 신청 바로가기", "/break/application")),
                        List.of("휴학신청", "휴학 신청", "휴학", "break application")
                ),
                new Topic(
                        ChatIntent.BREAK_LIST_STUDENT,
                        "휴학 내역 조회 안내(학생)",
                        List.of("포털 > MY > 휴학 내역 조회"),
                        List.of(new ChatLinkDto("휴학 내역 바로가기", "/break/list")),
                        List.of("휴학내역", "휴학 내역", "휴학조회", "휴학 신청 내역")
                ),
                new Topic(
                        ChatIntent.BREAK_LIST_STAFF,
                        "휴학 처리 안내(교직원)",
                        List.of("포털 > 학사 관리 > 휴학 처리"),
                        List.of(new ChatLinkDto("휴학 처리 바로가기", "/break/list/staff")),
                        List.of("휴학처리", "휴학 처리", "휴학승인", "휴학 신청 리스트")
                ),

                // ===== 등록금 =====
                new Topic(
                        ChatIntent.TUITION_LIST,
                        "등록금 내역 조회 안내",
                        List.of("포털 > MY > 등록금 내역 조회"),
                        List.of(new ChatLinkDto("등록금 내역 바로가기", "/tuition")),
                        List.of("등록금", "등록금내역", "납부내역", "tuition")
                ),
                new Topic(
                        ChatIntent.TUITION_PAYMENT,
                        "등록금 납부 고지서 안내",
                        List.of("포털 > MY > 등록금 납부 고지서"),
                        List.of(new ChatLinkDto("등록금 고지서 바로가기", "/tuition/payment")),
                        List.of("고지서", "등록금고지서", "등록금 납부", "payment")
                ),
                new Topic(
                        ChatIntent.TUITION_BILL_CREATE,
                        "등록금 고지서 생성 안내(교직원)",
                        List.of("포털 > 학사 관리 > 등록금 고지서 발송"),
                        List.of(new ChatLinkDto("고지서 생성/발송 바로가기", "/tuition/bill")),
                        List.of("고지서발송", "고지서 생성", "등록금 고지서 발송")
                ),

                // ===== 명단 조회 =====
                new Topic(
                        ChatIntent.PROFESSOR_LIST,
                        "교수 명단 조회 안내",
                        List.of("포털 > 학사 관리 > 교수 명단 조회"),
                        List.of(new ChatLinkDto("교수 명단 조회 바로가기", "/professor/list")),
                        List.of("교수명단", "교수 리스트", "교수 조회")
                ),
                new Topic(
                        ChatIntent.STUDENT_LIST,
                        "학생 명단 조회 안내",
                        List.of("포털 > 학사 관리 > 학생 명단 조회"),
                        List.of(new ChatLinkDto("학생 명단 조회 바로가기", "/student/list")),
                        List.of("학생명단", "학생 리스트", "학생 조회")
                ),

                // ===== 교수 =====
                new Topic(
                        ChatIntent.PROFESSOR_SUBJECT,
                        "내 강의 조회 안내(교수)",
                        List.of("포털 > 수업 > 내 강의 조회"),
                        List.of(new ChatLinkDto("내 강의 조회 바로가기", "/professor/subject")),
                        List.of("내강의", "내 강의", "교수강의", "professor subject")
                ),
                new Topic(
                        ChatIntent.PROFESSOR_EVALUATION,
                        "내 강의 평가 안내(교수)",
                        List.of("포털 > 수업 > 내 강의 평가"),
                        List.of(new ChatLinkDto("내 강의 평가 바로가기", "/professor/evaluation")),
                        List.of("강의평가", "내 강의 평가", "evaluation")
                ),

                // ===== 관리자(등록) =====
                new Topic(
                        ChatIntent.ADMIN_COLLEGE,
                        "단과대 등록 안내(관리자)",
                        List.of("포털 > 등록 > 단과 대학"),
                        List.of(new ChatLinkDto("단과대 등록 바로가기", "/admin/college")),
                        List.of("단과대", "단과 대학", "college")
                ),
                new Topic(
                        ChatIntent.ADMIN_DEPARTMENT,
                        "학과 등록 안내(관리자)",
                        List.of("포털 > 등록 > 학과"),
                        List.of(new ChatLinkDto("학과 등록 바로가기", "/admin/department")),
                        List.of("학과", "department")
                ),
                new Topic(
                        ChatIntent.ADMIN_ROOM,
                        "강의실 등록 안내(관리자)",
                        List.of("포털 > 등록 > 강의실"),
                        List.of(new ChatLinkDto("강의실 등록 바로가기", "/admin/room")),
                        List.of("강의실", "room")
                ),
                new Topic(
                        ChatIntent.ADMIN_SUBJECT,
                        "강의 등록 안내(관리자)",
                        List.of("포털 > 등록 > 강의"),
                        List.of(new ChatLinkDto("강의 등록 바로가기", "/admin/subject")),
                        List.of("강의등록", "강의 등록", "subject create")
                ),
                new Topic(
                        ChatIntent.ADMIN_COLLTUIT,
                        "단대별 등록금 등록 안내(관리자)",
                        List.of("포털 > 등록 > 단대별 등록금"),
                        List.of(new ChatLinkDto("단대별 등록금 바로가기", "/admin/colltuit")),
                        List.of("단대등록금", "단대별등록금", "colltuit")
                )
        );

        // 리스트 -> EnumMap 변환 (성능/가독성 좋음)
        Map<ChatIntent, Topic> map = new EnumMap<>(ChatIntent.class);
        for (Topic t : list) {
            map.put(t.intent(), t);
        }
        return map;
    }
}
