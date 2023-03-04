package org.springframework.samples.petclinic.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
@Profile("!project-loom")
public class Config {

	@Bean
	public Scheduler reactiveJdbcServiceScheduler(HikariDataSource dataSource) {
		var threadCount = dataSource.getMaximumPoolSize();
		return Schedulers.newBoundedElastic(threadCount, Integer.MAX_VALUE, "reactiveJdbcServiceScheduler");
	}

}
