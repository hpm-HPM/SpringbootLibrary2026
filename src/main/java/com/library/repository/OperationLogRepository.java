package com.library.repository;

import com.library.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OperationLogRepository extends JpaRepository<OperationLog, String>,
        JpaSpecificationExecutor<OperationLog> {
}