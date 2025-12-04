package com.green.university.config;

import com.green.university.config.security.JwtAuthenticationFilter;
import com.green.university.utils.Define;
import jakarta.servlet.http.HttpServletResponse;
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
                .httpBasic(basic -> basic.disable())
                // logout 설정
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")   // 프론트에서 호출할 로그아웃 URL
                        .invalidateHttpSession(true)     // 세션 무효화(STATELESS지만 혹시 모를 세션 제거)
                        .clearAuthentication(true)       // SecurityContext 인증정보 제거
                        .deleteCookies(
                                "JSESSIONID",
                                "remember-me",
                                "auth_code",
                                "Authorization"
                        )
                        .logoutSuccessHandler((req, res, auth) ->
                                res.setStatus(HttpServletResponse.SC_OK))
                );

        http.authorizeHttpRequests(auth -> auth
                // CORS preflight(OPTIONS) 전부 허용
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()

                // 로그인, 메인, 에러, 정적 리소스 모두 허용
                // ★ AuthController 기준으로 URL도 맞춰 주세요
                .requestMatchers(
                        "/api/auth/login",
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }
}
