package com.green.university.domain.scholarship.repository;

import com.green.university.domain.scholarship.entity.Scholarship;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScholarshipRepository extends JpaRepository<Scholarship,Long> {

}
