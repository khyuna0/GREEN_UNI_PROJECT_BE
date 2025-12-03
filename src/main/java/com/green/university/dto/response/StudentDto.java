package com.green.university.dto.response;

import com.green.university.entity.Student;
import lombok.Data;

@Data
public class StudentDto {
    private Long id;
    private String name;
    private String email;
    private String departmentName;

    public static StudentDto fromEntity(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setName(student.getName());
        dto.setEmail(student.getEmail());
        if (student.getDepartment() != null) {
            dto.setDepartmentName(student.getDepartment().getName());
        }
        return dto;
    }
}

