package com.green.university.dto.response;

import com.green.university.entity.Department;
import com.green.university.entity.Student;
import lombok.Data;

import java.time.LocalDate;


@Data
public class StudentDto { // 원 엔티티 필드와 맞춤

    private Long id;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String tel;
    private String email;
    private Department department;  // 엔티티 그대로
    private Long grade;
    private Long semester;
    private LocalDate entranceDate;
    private LocalDate graduationDate;

    private String departmentName;

    public static StudentDto fromEntity(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setName(s.getName());
        dto.setBirthDate(s.getBirthDate());
        dto.setGender(s.getGender());
        dto.setAddress(s.getAddress());
        dto.setTel(s.getTel());
        dto.setEmail(s.getEmail());
        dto.setDepartment(s.getDepartment()); // null이면 그냥 null
        dto.setGrade(s.getGrade());
        dto.setSemester(s.getSemester());
        dto.setEntranceDate(s.getEntranceDate());
        dto.setGraduationDate(s.getGraduationDate());
        if(s.getDepartment() != null) {
            dto.setDepartmentName(s.getDepartment().getName());
        } else {
            dto.setDepartmentName(null);
        }

        return dto;
    }
}


