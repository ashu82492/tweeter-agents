package com.nexus.app.config;

import com.nexus.identity.domain.User;
import com.nexus.identity.domain.UserType;
import com.nexus.identity.repository.UserRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.nexus.app.metrics.ErrorMetricsInterceptor;

@Configuration
public class ApplicationConfiguration {
    @Bean Clock clock() { return Clock.systemUTC(); }
    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean int recentTweetCount() { return 20; }

    @Bean WebMvcConfigurer errorMetricsConfigurer(ErrorMetricsInterceptor errorMetricsInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(errorMetricsInterceptor);
            }
        };
    }

    @Bean CommandLineRunner seedAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock,
                                     @Value("${nexus.admin.username:admin}") String username,
                                     @Value("${nexus.admin.password:admin-password}") String password,
                                     @Value("${nexus.admin.display-name:Nexus Administrator}") String displayName) {
        return ignored -> userRepository.findByUsername(username).orElseGet(() -> {
            Instant now = clock.instant();
            return userRepository.save(new User(UUID.randomUUID(), username, passwordEncoder.encode(password), displayName,
                    UserType.ADMIN, true, null, now, now));
        });
    }
}