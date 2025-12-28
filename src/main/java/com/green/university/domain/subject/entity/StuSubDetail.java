package com.green.university.domain.subject.entity;

import com.green.university.domain.student.entity.Student;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class StuSubDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stu_sub_id")
    private StuSub stuSub;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;

    private Long absent;
    private Long lateness;
    private Long homework;
    private Long midExam;
    private Long finalExam;
    private Double convertedMark; // 환산점수
    private String letterGrade; // 등급 (단순 출력용)

    @Column(nullable = false)
    private boolean finalized = false; // 성적 최종 확정 여부

    // 과제 + 중간 + 기말 = 평균 점수 계산 로직
    public double getCalculatedAvg() {
        double hw = this.homework == null ? 0 : this.homework;
        double mid = this.midExam == null ? 0 : this.midExam;
        double fin = this.finalExam == null ? 0 : this.finalExam;
        return (hw + mid + fin) / 3.0;
    }

    // 총 결석 횟수 계산 (지각 3회 = 결석 1회)
    public long getCalculatedAbsent() {
        long abs = this.absent == null ? 0 : this.absent;
        long lat = this.lateness == null ? 0 : this.lateness;
        return abs + (lat / 3);
    }
}
