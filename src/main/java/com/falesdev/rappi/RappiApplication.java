package com.falesdev.rappi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RappiApplication {

	public static void main(String[] args) {
		SpringApplication.run(RappiApplication.class, args);
	}

}
