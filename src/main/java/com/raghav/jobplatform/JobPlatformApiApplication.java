package com.raghav.jobplatform;

import com.raghav.jobplatform.config.JobApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(JobApiProperties.class)
@SpringBootApplication
public class JobPlatformApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobPlatformApiApplication.class, args);
	}

}
