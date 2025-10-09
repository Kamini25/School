package com.example.School.Config;



import com.example.School.Security.JwtAuthFilter;
import com.example.School.Service.CustomUserDetailsService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    // @Autowired
    // private FilterChainProxy filterChainProxy;

    /**
     * By default, Spring Security sets X-Frame-Options: DENY to prevent your app from being displayed in an <iframe> (for security against clickjacking).
      *The H2 Console requires being displayed in a frame (iframe) in the browser.
      *Disabling frame options i.e X-Frame-Options ka header nai set hga, this allows the H2 Console (/h2-console) to work properly. 
     * 
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.disable())) //
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() //to permit localhost with h2-console as endpoint
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN","USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationConfiguration is a Spring Security class that is registered as a bean by the framework.
      * When you declare it as a parameter in a @Bean method, Spring injects the existing AuthenticationConfiguration bean for you.
     * 
     * @param config
     * @return
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    //TODO yaha par config and authentication config ki value chk kar

//     @PostConstruct
//     public void printFilters() {
//     filterChainProxy.getFilterChains().forEach(chain -> {
//         chain.getFilters().forEach(f -> System.out.println(f.getClass().getSimpleName()));
//     });
// }
}
