package com.reservas.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;
	private final UserDetailsService userDetailsService;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());
		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
		http.csrf(csrf -> csrf.disable())
		    .cors(cors -> cors.configurationSource(request -> {
			    CorsConfiguration config = new CorsConfiguration();
			    config.setAllowedOrigins(List.of("http://localhost:3000"));
			    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
			    config.setAllowedHeaders(List.of("*"));
			    config.setAllowCredentials(true);
			    return config;
		    }))
		    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		    .authorizeHttpRequests(auth -> auth
				                           // --- ROTAS PÚBLICAS ---
				                           .requestMatchers("/auth/**").permitAll() // Login e Registro (USER)
				                           .requestMatchers(HttpMethod.GET, "/leases/**").permitAll() // Ver/Buscar locações (Leases)

				                           // --- ROTAS DE ADMIN ---
				                           // Somente ADMIN pode gerenciar os Leases (Tipos de Locação)
				                           .requestMatchers(HttpMethod.POST, "/leases").hasRole("ADMIN")
				                           .requestMatchers(HttpMethod.PUT, "/leases/**").hasRole("ADMIN")
				                           .requestMatchers(HttpMethod.DELETE, "/leases/**").hasRole("ADMIN")

				                           // --- ROTAS DE USUÁRIO AUTENTICADO (USER ou ADMIN) ---
				                           .requestMatchers(HttpMethod.POST, "/leases/hire-lease/**").authenticated()
				                           .requestMatchers(HttpMethod.PUT, "/user/**").authenticated()

				                           .requestMatchers(
						                           "/v3/api-docs/**",
						                           "/swagger-ui.html",
						                           "/swagger-ui/**",
						                           "/webjars/**"
				                           ).permitAll()
				                           .anyRequest().authenticated()
		                          )
		    .authenticationProvider(authenticationProvider())
		    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}
