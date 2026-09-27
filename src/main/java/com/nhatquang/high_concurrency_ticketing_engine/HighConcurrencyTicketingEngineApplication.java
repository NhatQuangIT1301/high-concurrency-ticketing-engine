package com.nhatquang.high_concurrency_ticketing_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching 
@SpringBootApplication
public class HighConcurrencyTicketingEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(HighConcurrencyTicketingEngineApplication.class, args);
	}

}
