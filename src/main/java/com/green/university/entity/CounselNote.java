package com.green.university.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 방 코드(1111)
    @Column(name = "room_code", nullable = false, length = 50)
    private String roomCode;

    // 교수 메모
    @Column(name = "professor_note", columnDefinition = "TEXT")
    private String professorNote;

    // 학생 메모
    @Column(name = "student_note", columnDefinition = "TEXT")
    private String studentNote;


    @PrePersist
    public void onCreate() {
        if (professorNote == null) professorNote = "";
        if (studentNote == null) studentNote = "";
    }

}
