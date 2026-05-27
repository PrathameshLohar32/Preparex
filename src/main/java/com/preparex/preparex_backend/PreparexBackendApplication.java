package com.preparex.preparex_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PreparexBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PreparexBackendApplication.class, args);
	}

}
