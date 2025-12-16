package com.green.university.domain.admin.service;

import com.green.university.domain.admin.entity.SugangPeriod;
import com.green.university.domain.admin.repository.SugangPeriodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class SugangPeriodService {
    @Autowired
    private SugangPeriodRepository sugangPeriodRepository;

    // 현재 상태 조회
    public int getCurrentStatus() {
        return sugangPeriodRepository.findFirstByOrderByIdDesc()
                .map(SugangPeriod::getStatus)
                .orElse(2); // 기본값: 종료 상태
    }

    // 상태 업데이트
    public void updateStatus(int newStatus) {
        if (newStatus < 0 || newStatus > 2) {
            throw new IllegalArgumentException("상태는 0, 1, 2만 가능");
        }

        SugangPeriod period = sugangPeriodRepository.findFirstByOrderByIdDesc()
                .orElse(new SugangPeriod());

        period.setStatus(newStatus);
        period.setUpdatedAt(LocalDateTime.now());
        sugangPeriodRepository.save(period);
    }
}
