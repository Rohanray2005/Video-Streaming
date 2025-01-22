package com.stream.app.stream_spring_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class StreamSpringBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(StreamSpringBackendApplication.class, args);
	}

}
