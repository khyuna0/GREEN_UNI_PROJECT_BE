package com.green.university.domain.university.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Room {

    @Id // 엔티티 필드 수정함
    @Column(length = 5)
    private String id;   // PK = 문자열 그대로 사용

    @ManyToOne
    @JoinColumn(name = "college_id")
    private College college;
}
