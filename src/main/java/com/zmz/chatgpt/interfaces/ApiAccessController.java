package com.zmz.chatgpt.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ApiAccessController {

    @GetMapping("/verify")
    public ResponseEntity<String> verify(String token) {
        log.info("验证 token：{}", token);
        if ("success".equals(token)){
            return ResponseEntity.status(HttpStatus.OK).body("操作成功");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("验证失败");
        }
    }

    @GetMapping("/success")
    public String success(){
        return "test success by api - 20231205-1823";
    }



}
