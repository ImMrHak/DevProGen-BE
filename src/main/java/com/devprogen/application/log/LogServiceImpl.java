package com.devprogen.application.log;

import com.devprogen.domain.log.service.LogDomainService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LogServiceImpl implements LogService {
    private final LogDomainService logDomainService;
}
