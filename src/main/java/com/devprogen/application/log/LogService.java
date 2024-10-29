package com.devprogen.application.log;

import com.devprogen.domain.log.model.Log;

import java.util.List;

public interface LogService {
    Long countAllLog();
    List<Log> searchAllLog();
}
