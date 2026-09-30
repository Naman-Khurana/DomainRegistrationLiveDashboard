package com.project.DomainRegistrationLive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class DomainRegistrationLiveApplication {

	public static void main(String[] args) {
		SpringApplication.run(DomainRegistrationLiveApplication.class, args);
	}

}
