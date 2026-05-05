package it.generation.suonacongigi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ARCHITETTURA: The Gatekeeper (Interceptor Pattern).
 * Questo filtro è posizionato "davanti" ai controller. Una sorta di dogana 
 * che controlla il passaporto (Token) di ogni passeggero (Richiesta).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractToken(request);

            // MECCANICA: Stateless Authentication.
            // Se c'è un token e il "posto" in memoria (SecurityContext) è vuoto...
            if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtUtil.extractUsername(token);

                if (username != null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.isTokenValid(token, userDetails)) {
                        // REFLECTION & CONTEXT: Creiamo il guscio di autenticazione.
                        UsernamePasswordAuthenticationToken authToken =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails, 
                                        null, 
                                        userDetails.getAuthorities()
                                );
                        
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // POPOLAMENTO THREAD-LOCAL: Da questo momento, l'utente è "loggato" per questa richiesta.
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            }
        } catch (Exception e) {
            // Defensive Logging: Non fermiamo la richiesta, lasciamo che sia il Config a dare 403.
            logger.error("Errore durante l'autenticazione JWT: " + e.getMessage());
        }

        // Proseguiamo lungo la catena dei filtri.
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        // PERFORMANCE: Escludiamo i percorsi pubblici per non sprecare cicli di CPU.
        return path.startsWith("/v3/api-docs")  || 
            path.startsWith("/swagger-ui")      || 
            path.startsWith("/api/auth");
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}