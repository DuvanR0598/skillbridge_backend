package com.udea.skillbridge.seguridad.filter;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.udea.skillbridge.seguridad.service.JwtService;
import com.udea.skillbridge.seguridad.service.UserDetailsServiceImpl;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepta cada request HTTP.
 * Si hay un JWT válido en el header Authorization,
 * establece el usuario autenticado en el SecurityContext.
 *
 * OncePerRequestFilter garantiza que se ejecuta solo una vez por request.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	
	private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extraer el token del header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            // 2. Extraer el idUsuario del token
            Long idUsuario = jwtService.extractUserId(jwt);

            // 3. Solo procesar si no hay autenticación previa en el contexto
            if (idUsuario != null && SecurityContextHolder.getContext()
                    .getAuthentication() == null) {

                UserDetails userDetails =
                        userDetailsService.loadUserById(idUsuario);

                // 4. Validar el token contra el usuario cargado
                if (jwtService.isTokenValid(jwt, (com.udea.skillbridge.seguridad.entity.UsuarioEntity) userDetails)) {

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 5. Establecer la autenticación en el contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            log.warn("No se pudo procesar el JWT: {}", ex.getMessage());
            // No lanzamos excepción — dejamos que Spring Security maneje el 401
        }

        filterChain.doFilter(request, response);
    }

}
