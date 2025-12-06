package com.example.productservice.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
   
   @Value("${jwt.secret}")
   private String jwtSecret;

   @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException 
   {
      System.out.println("JWT Filter active for: " + request.getRequestURI());
      String header = request.getHeader("Authorization");
      if (header != null && header.startsWith("Bearer ")) {
         String token = header.substring(7);
         try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            String jwt = Jwts.builder()
                  .setSubject("phuc")
                  .claim("userType", "USER")
                  .claim("userId", 1)
                  .setIssuedAt(new Date())
                  .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1 giờ
                  .signWith(key)
                  .compact();

            System.out.println("JWT token: " + jwt);


            Claims claims = Jwts.parserBuilder()
               .setSigningKey(key)
               .build()
               .parseClaimsJws(token)
               .getBody();

            String username = claims.getSubject();
            String userType = claims.get("userType", String.class);
            System.out.println("username: " + username);
            System.out.println("userType: " + userType);
            System.out.println("id: " + claims.get("userId", Integer.class));
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + userType.toUpperCase()));
            UsernamePasswordAuthenticationToken auth =
               new UsernamePasswordAuthenticationToken(username, null, authorities);
            auth.setDetails(claims);
            SecurityContextHolder.getContext().setAuthentication(auth);
         } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
         }
      }

      filterChain.doFilter(request, response);
   }
}
