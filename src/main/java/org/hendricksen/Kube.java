package org.hendricksen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
@RestController
public class Kube {
    @GetMapping("/hello")
    public String hello() {
        String podName = System.getenv().getOrDefault("HOSTNAME", "unknown-pod");
        return "Hello, Kubernetes! From pod: " + podName;
    }

    public static void main(String[] args) {
        SpringApplication.run(Kube.class, args);
    }
}