package com.green.university.config;

import com.green.university.utils.Define;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        http.authorizeHttpRequests(auth -> auth
                //  CORS preflight(OPTIONS) 전부 허용
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // 로그인, 메인, 에러, 정적 리소스 모두 허용
                .requestMatchers(
                        "/api/login",
                        "/login",
                        "/",
                        "/error",
                        "/images/**"
                ).permitAll()

                // 학생 전용
                .requestMatchers(Define.STUDENT_PATHS).hasRole("STUDENT")
                // 교수 전용
                .requestMatchers(Define.PROFESSOR_PATHS).hasRole("PROFESSOR")
                // 직원 전용
                .requestMatchers(Define.STAFF_PATHS).hasRole("STAFF")
                .anyRequest().authenticated()
        );

        // JWT 필터 등록 (UsernamePasswordAuthenticationFilter 앞에)
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // AuthenticationManager 필요하면 (id+password 인증용)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
