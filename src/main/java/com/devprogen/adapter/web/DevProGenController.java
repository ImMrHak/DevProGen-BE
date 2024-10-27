package com.devprogen.adapter.web;

import com.devprogen.adapter.wrapper.ResponseWrapper;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class DevProGenController {
    @GetMapping
    public ResponseEntity<?> HelloWorld(){
        return ResponseEntity.status(HttpStatus.OK).body(ResponseWrapper.success("Hello World!"));
    }

    @GetMapping("/error")
    public ResponseEntity<?> Error(){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseWrapper.error("Internal Error"));
    }
}
