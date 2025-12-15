package com.green.university.infra.ai;

import com.green.university.infra.ai.dto.response.DropoutRiskRowDto;
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
