package com.safari.safarims.config;

import com.safari.safarims.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/packages").permitAll() // Allow tourists to browse packages
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/", "/login.html", "/signup.html", "/reset-password.html").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                // Admin endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Tourist endpoints
                .requestMatchers("/api/v1/tourists/**").hasAnyRole("TOURIST", "ADMIN")
                .requestMatchers("/api/v1/bookings/**").hasAnyRole("TOURIST", "BOOKING_OFFICER", "ADMIN")

                // Staff endpoints
                .requestMatchers("/api/v1/crew/**").hasAnyRole("TOUR_CREW_MANAGER", "ADMIN")
                .requestMatchers("/api/v1/maintenance/**").hasAnyRole("MAINTENANCE_OFFICER", "ADMIN")
                .requestMatchers("/api/v1/packages/**").hasAnyRole("TOUR_PACKAGE_BUILDER", "ADMIN")
                .requestMatchers("/api/v1/tickets/**").hasAnyRole("DRIVER", "GUIDE", "MAINTENANCE_OFFICER", "ADMIN")
                .requestMatchers("/api/v1/allocations/**").hasAnyRole("TOUR_CREW_MANAGER", "BOOKING_OFFICER", "ADMIN")

                // Default - require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
