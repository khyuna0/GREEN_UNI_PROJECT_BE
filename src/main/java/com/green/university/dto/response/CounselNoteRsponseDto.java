package com.green.university.dto.response;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CounselNoteRsponseDto {

    private String professorNote;
    private String studentNote;

}
