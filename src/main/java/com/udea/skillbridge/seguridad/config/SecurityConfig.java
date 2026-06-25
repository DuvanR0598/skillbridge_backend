package com.udea.skillbridge.seguridad.config;

import java.util.List;

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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
    private final JwtAuthenticationEntryPoint jwtAuthEntryPoint;

    /**
     * Orígenes permitidos para CORS (separados por coma). Configurable por la
     * variable de entorno APP_CORS_ALLOWED_ORIGINS. Por defecto, el dev server
     * de Angular. En producción se añade el dominio del frontend desplegado, ej:
     * APP_CORS_ALLOWED_ORIGINS=http://localhost:4200,https://mi-frontend.netlify.app
     */
    @org.springframework.beans.factory.annotation.Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
        	.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Desactivar CSRF — usamos JWT stateless
            .csrf(AbstractHttpConfigurer::disable)

            // Sin sesiones HTTP — cada request es independiente
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Token ausente/expirado → 401 JSON (no redirige a Google)
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(jwtAuthEntryPoint)
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

                // Avatares e imágenes subidas — accesibles públicamente (se cargan
                // desde etiquetas <img>, que no envían el token de autorización).
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

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
                // Forzar el selector de cuentas de Google en cada login
                .authorizationEndpoint(authorization -> authorization
                    .authorizationRequestResolver(
                        authorizationRequestResolver(clientRegistrationRepository)))
                .successHandler(oAuth2SuccessHandler)
            )

            // ── Proveedor de autenticación ───────────────────────────
            .authenticationProvider(authenticationProvider())

            // ── Agregar filtro JWT antes del filtro de Spring Security
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Resolver que añade {@code prompt=select_account} a la petición de
     * autorización de Google. Así el usuario siempre ve el selector de
     * cuentas en lugar de autenticarse en silencio con la sesión activa.
     */
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
            new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(customizer ->
            customizer.additionalParameters(params ->
                params.put("prompt", "select_account")));

        return resolver;
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
    
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Orígenes permitidos: configurables por env var (lista separada por comas).
        // Dev por defecto: http://localhost:4200. Prod: añadir el dominio del frontend.
        config.setAllowedOrigins(
            java.util.Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(o -> !o.isBlank())
                .toList()
        );

        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"
        ));

        config.setAllowedHeaders(List.of(
            "Authorization", 
            "Content-Type", 
            "Accept",
            "X-Requested-With",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        config.setExposedHeaders(List.of(
                "Authorization"  // Para enviar el token JWT en respuesta
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
