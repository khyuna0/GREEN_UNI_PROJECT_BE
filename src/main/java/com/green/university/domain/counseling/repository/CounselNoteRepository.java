package com.green.university.domain.counseling.repository;

import com.green.university.domain.counseling.entity.CounselNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CounselNoteRepository extends JpaRepository<CounselNote, Long> {

    Optional<CounselNote> findByRoomCode(String roomCode);

}
