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
public class PersistenceConfig implements AuditorAware<Long> {
	@Override
	public Optional<Long> getCurrentAuditor() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
			return Optional.empty(); // 유저 정보 없음
		}

		CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();

		return Optional.of(Long.parseLong(principal.getUsername()));
	}
}
