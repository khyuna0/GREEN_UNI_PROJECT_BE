package com.green.university.controller;

import com.green.university.config.security.CustomUserDetails;
import com.green.university.dto.CounselNoteSaveDto;
import com.green.university.dto.response.CounselNoteRsponseDto;
import com.green.university.service.CounselNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/counsel")
public class CounselNoteController {

    private final CounselNoteService service;

    @GetMapping("/note")
    public ResponseEntity<CounselNoteRsponseDto> get(@RequestParam String roomCode) {
        return ResponseEntity.ok(service.getNotes(roomCode));
    }

    @PostMapping("/note")
    public ResponseEntity<Void> save(@RequestParam String roomCode,
                                     @RequestBody CounselNoteSaveDto dto,
                                     @AuthenticationPrincipal CustomUserDetails me) {
        service.saveMyNote(roomCode, dto, me);
        return ResponseEntity.ok().build();
    }
}
