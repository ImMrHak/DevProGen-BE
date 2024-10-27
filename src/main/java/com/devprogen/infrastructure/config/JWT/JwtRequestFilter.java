package com.devprogen.infrastructure.config.JWT;

import com.devprogen.domain.user.model.User;
import com.devprogen.domain.user.service.UserDomainService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserDomainService userDomainService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userName;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        jwtToken = authHeader.substring(7); // Skip "Bearer " prefix
        userName = jwtUtil.extractUsername(jwtToken);

        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load user details using a UserDetails instance
            User userDetails = userDomainService.loadUserByUsername(userName);
            if (userDetails != null && jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {

                // Create authentication token
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Set the authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(token);
            }
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }
}
