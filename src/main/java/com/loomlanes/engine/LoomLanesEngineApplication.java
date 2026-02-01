package com.loomlanes.engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoomLanesEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoomLanesEngineApplication.class, args);
	}

}
