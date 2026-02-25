package com.feverup.plans;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.feverup.plans")
@EnableScheduling
@EnableCaching
public class PlanEventApplication {
    public static void main(String[] args) {
        System.setProperty("io.netty.resolver.dns.macos.native", "false");
        SpringApplication.run(PlanEventApplication.class, args);
    }
}
