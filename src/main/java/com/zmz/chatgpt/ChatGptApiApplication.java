package com.zmz.chatgpt;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
public class ChatGptApiApplication {

    public static void main(String[] args) throws UnknownHostException {
        configDefaultEnv();
        Environment env = SpringApplication.run(ChatGptApiApplication.class).getEnvironment();
        String port = env.getProperty("server.port", "8080");
        String healthPort = env.getProperty("management.server.port", "8081");
        String hostAddress = InetAddress.getLocalHost().getHostAddress();
        log.info("Access URLS:\n---------------------------\n\t"
                        + "Local: \t\thttp://127.0.0.1:{}\n\t"
                        + "External: \thttp://{}:{}\n\t"
                        + "health: \thttp://{}:{}/health\n---------------------------",
                port,
                hostAddress, port,
                hostAddress, healthPort);
    }

    private static void configDefaultEnv() {
        String env = System.getProperty("env");
        if (StringUtils.isBlank(env)) {
            env = "local";
            System.setProperty("env", env);
        }
    }

}
