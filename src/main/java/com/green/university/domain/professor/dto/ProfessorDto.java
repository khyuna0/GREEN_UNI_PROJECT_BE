package com.green.university.domain.professor.dto;

import com.green.university.domain.university.entity.Department;
import com.green.university.domain.professor.entity.Professor;
import lombok.Data;

import java.time.LocalDate;
@Data
public class ProfessorDto {

    private Long id;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
    private Department department;
    private LocalDate hireDate;

    public static ProfessorDto fromEntity(Professor p) {
        ProfessorDto dto = new ProfessorDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setBirthDate(p.getBirthDate());
        dto.setGender(p.getGender());
        dto.setAddress(p.getAddress());
        dto.setTel(p.getTel());
        dto.setEmail(p.getEmail());
        dto.setDepartment(p.getDepartment());
        dto.setHireDate(p.getHireDate());
        return dto;
    }
}

