package com.green.university.repository;

import com.green.university.entity.CounselNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface CounselNoteRepository extends JpaRepository<CounselNote, Long> {

    Optional<CounselNote> findByRoomCode(String roomCode);

}
