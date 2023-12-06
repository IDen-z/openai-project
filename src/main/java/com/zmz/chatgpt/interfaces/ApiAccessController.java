package com.zmz.chatgpt.interfaces;

import com.zmz.chatgpt.domain.security.service.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
public class ApiAccessController {

    /**
     * 认证接口
     * http://localhost:8080/authorize?username=xfg&password=123
     */
    @RequestMapping("/authorize")
    public ResponseEntity<Map<String, String>> authorize(String username, String password) {
        Map<String, String> map = new HashMap<>();
        // 模拟账号和密码校验
        if (!"secrets".equals(username) || !"123456".equals(password)) {
            map.put("msg", "用户名密码错误");
            return ResponseEntity.ok(map);
        }
        // 校验通过生成token
        JwtUtil jwtUtil = new JwtUtil();
        Map<String, Object> chaim = new HashMap<>();
        chaim.put("username", username);
        String jwtToken = jwtUtil.encode(username, 5 * 60 * 1000, chaim);
        map.put("msg", "授权成功");
        map.put("token", jwtToken);
        // 返回token码
        return ResponseEntity.ok(map);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify() {
        log.info("验证 token");
        return ResponseEntity.status(HttpStatus.OK).body("verify success!");
    }

    @GetMapping("/success")
    public String success(){
        return "test success by api - 20231205-1823";
    }



}
