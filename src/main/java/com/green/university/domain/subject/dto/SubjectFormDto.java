package com.green.university.domain.subject.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SubjectFormDto {

    private Long id;

    /* 강의명 */
    @NotEmpty(message = "강의명을 입력해 주세요")
    @Size(min = 2, max = 20, message = "강의명은 2~20자 이내여야 합니다")
    private String name;

    /* 담당 교수 */
    private Long professorId;
    @NotEmpty(message = "담당 교수를 입력해 주세요")
    private String professorName;

    /* 강의실 */
    @NotEmpty(message = "강의실을 입력해 주세요")
    @Size(max = 10, message = "강의실은 10자 이내로 입력해 주세요")
    private String roomId;

    /* 학과 */
    private Long deptId;

    @NotEmpty(message = "학과 이름을 입력해 주세요")
    private String deptName;

    /* 이수 구분 */
    @NotEmpty(message = "이수 구분을 선택해 주세요")
    @Pattern(regexp = "^(전공|교양)$", message = "이수 구분은 전공 또는 교양만 가능합니다")
    private String type;

    /* 연도 */
    @NotNull(message = "연도를 입력해 주세요")
    @Min(value = 2000, message = "연도는 2000년 이상이어야 합니다")
    @Max(value = 2100, message = "연도 형식이 올바르지 않습니다")
    private Long subYear;

    /* 학기 */
    @NotNull(message = "학기를 입력해 주세요")
    @Min(value = 1, message = "학기는 1 또는 2만 가능합니다")
    @Max(value = 2, message = "학기는 1 또는 2만 가능합니다")
    private Long semester;

    /* 요일 */
    @NotEmpty(message = "요일을 선택해 주세요")
    @Pattern(
            regexp = "^(월|화|수|목|금)$",
            message = "요일은 월~금만 선택 가능합니다"
    )
    private String subDay;

    /* 시작 시간 */
    @NotNull(message = "시작 시간을 입력해 주세요")
    @Min(value = 9, message = "시작 시간은 9시 이상이어야 합니다")
    @Max(value = 18, message = "시작 시간은 18시 이하만 가능합니다")
    private Long startTime;

    /* 종료 시간 */
    @NotNull(message = "종료 시간을 입력해 주세요")
    @Min(value = 10, message = "종료 시간은 10시 이상이어야 합니다")
    @Max(value = 19, message = "종료 시간은 19시 이하만 가능합니다")
    private Long endTime;

    /* 이수 학점 */
    @NotNull(message = "이수 학점을 입력해 주세요")
    @Min(value = 1, message = "이수 학점은 1 이상이어야 합니다")
    @Max(value = 6, message = "이수 학점은 6 이하만 가능합니다")
    private Long credits;

    /* 정원 */
    @NotNull(message = "정원을 입력해 주세요")
    @Min(value = 1, message = "정원은 1명 이상이어야 합니다")
    @Max(value = 300, message = "정원은 300명 이하만 가능합니다")
    private Long capacity;
}
