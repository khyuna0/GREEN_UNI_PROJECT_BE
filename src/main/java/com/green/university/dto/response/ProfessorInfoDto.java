package com.green.university.dto.response;

import com.green.university.entity.Professor;
import com.green.university.entity.Subject;
import com.green.university.entity.SyllaBus;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

//import java.sql.Date; 타입 오류 때문에 위로 변경

@Data
public class ProfessorInfoDto {

	private Long id;
	private String name;
	private LocalDate birthDate;
	private String gender;
	private String address;
	private String tel;
	private String email;
	private Long deptId;
	private LocalDate hireDate;
	private String deptName;
	private String collegeName;

    public ProfessorInfoDto(Professor p) {

        // professor 부분
        this.id = p.getId();
        this.name = p.getName();
        this.birthDate = p.getBirthDate();
        this.gender = p.getGender();
        this.address = p.getAddress();
        this.tel = p.getTel();
        this.email = p.getEmail();
        this.hireDate = p.getHireDate();

        // college 부분
        this.deptName = p.getDepartment().getName();
        this.collegeName = p.getDepartment().getCollege().getName();

    }

}
