package com.green.university.domain.counseling.service;

import com.green.university.domain.counseling.dto.CounselNoteSaveDto;
import com.green.university.domain.counseling.dto.CounselNoteRsponseDto;
import com.green.university.domain.counseling.entity.CounselNote;
import com.green.university.domain.counseling.repository.CounselNoteRepository;
import com.green.university.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CounselNoteService {

    private final CounselNoteRepository repo;

    @Transactional(readOnly = true)
    public CounselNoteRsponseDto getNotes(String roomCode) {
        CounselNote note = repo.findByRoomCode(roomCode)
                .orElseGet(() -> repo.save(CounselNote.builder().roomCode(roomCode).build()));

        return new CounselNoteRsponseDto(
                note.getProfessorNote() == null ? "" : note.getProfessorNote(),
                note.getStudentNote() == null ? "" : note.getStudentNote()
        );
    }

    @Transactional
    public void saveMyNote(String roomCode, CounselNoteSaveDto req, CustomUserDetails me) {
        CounselNote note = repo.findByRoomCode(roomCode)
                .orElseGet(() -> repo.save(CounselNote.builder().roomCode(roomCode).build()));

        String content = (req.getContent() == null) ? "" : req.getContent();

        // userRole만 보고 교수/학생 칸에 저장
        String role = me.getUserRole(); // "professor" / "student" 같은 값
        if ("PROFESSOR".equalsIgnoreCase(role)) {
            note.setProfessorNote(content);
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            note.setStudentNote(content);
        }

        repo.save(note);
    }
}
