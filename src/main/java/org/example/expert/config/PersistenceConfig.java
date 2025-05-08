package org.example.expert.config;

import java.util.Optional;

import org.example.expert.security.CustomUserPrincipal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing
public class PersistenceConfig {
	@Bean
	public AuditorAware<Long> auditorProvider() {
		return () -> Optional.of(Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName()));
	}
}
