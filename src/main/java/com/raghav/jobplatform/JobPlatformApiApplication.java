package com.raghav.jobplatform;

import com.raghav.jobplatform.config.JobApiProperties;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties(JobApiProperties.class)
@EnableScheduling
@SpringBootApplication
public class JobPlatformApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobPlatformApiApplication.class, args);
	}

	@Bean
	public CommandLineRunner initDatabase(JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS experience INTEGER");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS current_role_title VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS bio TEXT");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS linkedin VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS github VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS portfolio VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_roles VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS preferred_locations VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS remote_only BOOLEAN");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS salary_range VARCHAR(255)");
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN IF NOT EXISTS job_types VARCHAR(255)");
				System.out.println(">>> Database schema updated successfully with new columns!");
			} catch (Exception e) {
				System.err.println(">>> Database migration failed: " + e.getMessage());
			}
		};
	}
}
