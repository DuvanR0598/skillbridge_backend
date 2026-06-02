package com.udea.skillbridge.seguridad.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.udea.skillbridge.seguridad.filter.JwtAuthenticationFilter;
import com.udea.skillbridge.seguridad.oauth2.OAuth2AuthenticationSuccessHandler;
import com.udea.skillbridge.seguridad.service.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity           // Habilita @PreAuthorize en controllers y services
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final OAuth2AuthenticationSuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactivar CSRF — usamos JWT stateless
            .csrf(AbstractHttpConfigurer::disable)

            // Sin sesiones HTTP — cada request es independiente
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Reglas de autorización por URL
            .authorizeHttpRequests(auth -> auth

                // ── Públicos ─────────────────────────────────────────
                .requestMatchers(
                    "/auth/**",
                    "/oauth2/**",
                    "/login/oauth2/**"
                ).permitAll()

                // Swagger / OpenAPI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api-docs/**"
                ).permitAll()

                // ── Cuestionarios ────────────────────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/cuestionario").hasAnyRole("ADMIN", "COORDINADOR")
                .requestMatchers(HttpMethod.PATCH,
                    "/cuestionario/*/publicado").hasAnyRole("ADMIN", "COORDINADOR")
                .requestMatchers(HttpMethod.PATCH,
                    "/cuestionario/*/archivado").hasAnyRole("ADMIN", "COORDINADOR")
                .requestMatchers(HttpMethod.DELETE,
                    "/cuestionario/*").hasAnyRole("ADMIN", "COORDINADOR")
                .requestMatchers(HttpMethod.GET,
                    "/cuestionario/*/entregar").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.GET,
                    "/cuestionario/**").hasAnyRole("ADMIN", "COORDINADOR", "ESTUDIANTE")

                // ── Preguntas ────────────────────────────────────────
                .requestMatchers("/preguntas/**")
                    .hasAnyRole("ADMIN", "COORDINADOR")

                // ── Condiciones ──────────────────────────────────────
                .requestMatchers("/cuestionario/*/condicion_pregunta/**")
                    .hasAnyRole("ADMIN", "COORDINADOR")

                // ── Matriz y planes ──────────────────────────────────
                .requestMatchers("/cuestionario/*/puntuacion_matrix/**")
                    .hasAnyRole("ADMIN", "COORDINADOR")
                .requestMatchers("/puntuacion_matrix/**")
                    .hasAnyRole("ADMIN", "COORDINADOR")

                // ── Evaluaciones ─────────────────────────────────────
                .requestMatchers(HttpMethod.POST,
                    "/evaluacion/**").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.PATCH,
                    "/evaluacion/*/completo").hasRole("ESTUDIANTE")
                .requestMatchers(HttpMethod.GET,
                    "/evaluacion/**").hasAnyRole("ADMIN", "COORDINADOR", "ESTUDIANTE")

                // ── Analytics ────────────────────────────────────────
                .requestMatchers(
                    "/analitica/cuestioanrio/**").hasAnyRole("ADMIN", "COORDINADOR")
                .requestMatchers(
                    "/analitica/estudiante/**").authenticated()

                // ── Usuarios ─────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,
                    "/usuarios").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,
                    "/usuarios/*/roles").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,
                    "/usuarios/*/toggle-enabled").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET,
                    "/usuarios/me").authenticated()
                
                // ── Perfil ─────────────────────────────────────────────────
                .requestMatchers(
                    "/usuarios/me/perfil",
                    "/usuarios/me/perfil/estado",
                    "/usuarios/me/perfil/avatar"
                ).authenticated()
                
                .requestMatchers(HttpMethod.GET,
                	"/usuarios/*/perfil").hasAnyRole("ADMIN", "COORDINADOR")

            	// Programas de ingeniería — endpoint público
            	.requestMatchers(HttpMethod.GET,
            	    "/perfil/programas").permitAll()

                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()
            )

            // ── OAuth2 (Google) ──────────────────────────────────────
            .oauth2Login(oauth2 -> oauth2
                .successHandler(oAuth2SuccessHandler)
                // Spring maneja automáticamente el redirect a Google
            )

            // ── Proveedor de autenticación ───────────────────────────
            .authenticationProvider(authenticationProvider())

            // ── Agregar filtro JWT antes del filtro de Spring Security
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

}
