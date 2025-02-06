package it.cusc.acAnalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AcAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AcAnalyzerApplication.class, args);
    }
}
