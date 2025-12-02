package com.green.university.repository.interfaces;

import com.green.university.entity.Scholarship;
import com.green.university.entity.StuSch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * @author 서영
 *
 */


public interface ScholarshipRepository extends JpaRepository<Scholarship,Long> {

}
