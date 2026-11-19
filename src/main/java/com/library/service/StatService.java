package com.library.service;

import com.library.dto.StatsItem;
import com.library.repository.StatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StatService {

    private final StatRepository statRepository;

    public StatService(StatRepository statRepository) {
        this.statRepository = statRepository;
    }

    @Transactional(readOnly = true)
    public List<StatsItem> byType() {
        return map(statRepository.countByBookType());
    }

    @Transactional(readOnly = true)
    public List<StatsItem> byMajor() {
        return map(statRepository.countByUserMajor());
    }

    private List<StatsItem> map(List<Object[]> rows) {
        return rows.stream().map(row -> {
            String name = row[0] == null ? "未分类" : String.valueOf(row[0]);
            long count = row[1] instanceof Number number ? number.longValue() : 0L;
            return new StatsItem(name, count);
        }).toList();
    }
}