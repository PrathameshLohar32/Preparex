package com.preparex.preparex_backend.security;

import com.preparex.preparex_backend.constant.SecurityConstants;
import com.preparex.preparex_backend.exception.InvalidTokenException;
import com.preparex.preparex_backend.service.SessionService;
import com.preparex.preparex_backend.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter that runs once per request.
 * Validates the Bearer token, verifies the session is still active in Redis,
 * and populates the SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractBearerToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtUtil.validateAndExtractClaims(token);
            String sessionId = jwtUtil.extractSessionId(claims);
            String userIdStr = jwtUtil.extractUserId(claims);

            if (!sessionService.isSessionActive(sessionId)) {
                log.warn("JWT refers to an inactive session. sessionId={}", sessionId);
                filterChain.doFilter(request, response);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> roles = claims.get(SecurityConstants.CLAIM_ROLES, List.class);

            CustomUserDetails userDetails = new CustomUserDetails(
                    UUID.fromString(userIdStr),
                    claims.get(SecurityConstants.CLAIM_EMAIL, String.class),
                    sessionId,
                    roles != null ? roles : List.of(SecurityConstants.ROLE_USER)
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authenticated userId={} sessionId={}", userIdStr, sessionId);

        } catch (InvalidTokenException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(SecurityConstants.AUTHORIZATION_HEADER);
        if (StringUtils.hasText(header) && header.startsWith(SecurityConstants.BEARER_PREFIX)) {
            return header.substring(SecurityConstants.BEARER_PREFIX.length());
        }
        return null;
    }
}
