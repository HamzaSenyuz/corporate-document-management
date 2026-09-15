package com.hamza.document_management.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()          // login sayfası herkese açık
                        .requestMatchers("/approvals/**").hasRole("MANAGER")  // sadece yönetici
                        .anyRequest().authenticated()                   // geri kalan her şey giriş ister
                )
                .formLogin(form -> form
                        .loginPage("/login")                  // kendi login sayfamız
                        .defaultSuccessUrl("/documents", true) // giriş sonrası buraya git
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}