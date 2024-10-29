package com.devprogen.application.log;

import com.devprogen.domain.log.model.Log;
import com.devprogen.domain.log.service.LogDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogDomainService logDomainService;

    @Override
    public Long countAllLog() {
        return Long.parseLong(String.valueOf(logDomainService.findAll().size()));
    }

    @Override
    public List<Log> searchAllLog() {
        return logDomainService.findAll();
    }
}
