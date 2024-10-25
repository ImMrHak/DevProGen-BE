package com.devprogen.infrastructure.persistence;

import com.devprogen.domain.log.model.Log;
import com.devprogen.domain.log.repository.LogRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaLogRepository extends JpaRepository<Log, Long>, LogRepository {
}
