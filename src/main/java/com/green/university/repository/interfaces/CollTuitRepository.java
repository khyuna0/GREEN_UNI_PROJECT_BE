package com.green.university.repository.interfaces;

import com.green.university.dto.CollTuitFormDto;
import com.green.university.entity.CollTuit;
import com.green.university.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

/*
 *  박성희
 *  단과대별 등록금 repository
 */
public interface CollTuitRepository extends JpaRepository<CollTuit, Long> {
//    public int insert(CollTuitFormDto collTuitFormDto);
//    public List<CollTuitFormDto> selectByCollTuitDto();
//    public int deleteById(Integer collegeId);
//    public int updateByCollTuitDto(CollTuitFormDto collTuitFormDto);

    // 해당 단과대(College)가 이미 등록금이 있는지 여부 (중복 체크)
    boolean existsByCollege_Id(Long collegeId);

    // 단과대 기준으로 등록금 하나 찾기
    Optional<CollTuit> findByCollege_Id(Long collegeId);

    // 단과대 기준으로 등록금 삭제
    void deleteByCollege_Id(Long collegeId);
}
