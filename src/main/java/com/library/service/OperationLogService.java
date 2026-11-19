package com.library.service;

import com.library.common.PageResult;
import com.library.entity.OperationLog;
import com.library.repository.OperationLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class OperationLogService {

    private final OperationLogRepository logRepository;

    public OperationLogService(OperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    @Transactional
    public void save(OperationLog log) {
        logRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResult<OperationLog> search(int page, int size, String module, String keyword) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String moduleValue = ServiceSupport.text(module);
        String kw = ServiceSupport.text(keyword);
        Specification<OperationLog> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (moduleValue != null && !moduleValue.isEmpty()) {
                predicates.add(cb.equal(root.get("module"), moduleValue));
            }
            if (kw != null && !kw.isEmpty()) {
                String like = "%" + kw.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("action")), like),
                        cb.like(cb.lower(root.get("userName")), like),
                        cb.like(cb.lower(root.get("requestUri")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<OperationLog> result = logRepository.findAll(specification, pageable);
        return PageResult.of(result, pageNo, pageSize);
    }
}