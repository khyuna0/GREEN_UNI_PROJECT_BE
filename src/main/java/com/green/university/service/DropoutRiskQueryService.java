package com.green.university.service;

import com.green.university.dto.response.DropoutRiskRowDto;
import com.green.university.repository.DropoutRiskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DropoutRiskQueryService {

    private final DropoutRiskRepository dropoutRiskRepository;

    @Transactional(readOnly = true)
    public List<DropoutRiskRowDto> getRisksBySubject(Long subjectId) {
        return dropoutRiskRepository.findByStuSub_Subject_Id(subjectId)
                .stream()
                .map(DropoutRiskRowDto::from)
                .toList();
    }
}
