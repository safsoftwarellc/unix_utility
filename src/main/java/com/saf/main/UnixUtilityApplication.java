package com.saf.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@ComponentScan(basePackages = {"com.saf.unix.controller", "com.saf.unix"})
@EnableAutoConfiguration
@Configuration
public class UnixUtilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnixUtilityApplication.class, args);
	}

}
