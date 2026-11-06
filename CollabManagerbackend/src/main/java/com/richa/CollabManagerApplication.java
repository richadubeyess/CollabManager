package com.richa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CollabManagerApplication {
	private static final Logger logger =
			LoggerFactory.getLogger(CollabManagerApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(CollabManagerApplication.class, args);
	}

}
