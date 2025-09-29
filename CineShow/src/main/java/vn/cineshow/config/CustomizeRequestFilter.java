package vn.cineshow.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.cineshow.enums.TokenType;
import vn.cineshow.service.JWTService;
import vn.cineshow.service.impl.AccountDetailsService;

import java.io.IOException;

@Component
@Slf4j(topic = "CUSTOMIZE-REQUEST-FILTER")
@RequiredArgsConstructor

public class CustomizeRequestFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final AccountDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("=== JWT FILTER DEBUG ===");
        log.info("Request: {} {}", request.getMethod(), request.getRequestURI());

        //TODO: check authority by request url
        String authHeader = request.getHeader("Authorization");
        log.info("Authorization Header: '{}'", authHeader);
        log.info("Authorization Header length: {}", authHeader != null ? authHeader.length() : "null");

        if (authHeader == null) {
            log.warn("❌ No Authorization header found");
        } else if (authHeader.trim().equals("Bearer")) {
            log.warn("❌ Authorization header is just 'Bearer' without token");
        } else if (!authHeader.startsWith("Bearer ")) {
            log.warn("❌ Authorization header does not start with 'Bearer '");
        } else {
            String token = authHeader.substring(7);
            
            // Remove curly braces if present (Postman variable issue)
            if (token.startsWith("{") && token.endsWith("}")) {
                token = token.substring(1, token.length() - 1);
                log.info("🔧 Removed curly braces from token");
            }
            
            log.info("✅ Token extracted: {}...", token.substring(0, Math.min(20, token.length())));
            
            try {
                String username = jwtService.extractUsername(token, TokenType.ACCESS_TOKEN);
                log.info("✅ Username extracted: {}", username);
                
                if (username == null) {
                    log.warn("❌ Username is null from token");
                } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    log.info("✅ User already authenticated: {}", SecurityContextHolder.getContext().getAuthentication().getName());
                } else {
                    log.info("🔍 Loading user details for: {}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    log.info("✅ User details loaded: {}", userDetails.getUsername());
                    
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.info("✅ Authentication set in SecurityContext");
                }
            } catch (Exception e) {
                log.error("❌ JWT processing error: {}", e.getMessage());
                log.error("❌ Exception type: {}", e.getClass().getSimpleName());
                e.printStackTrace();
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired JWT");
                return;
            }
        }
        
        log.info("=== END JWT FILTER DEBUG ===");
        filterChain.doFilter(request, response);
    }

}
