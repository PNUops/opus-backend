package com.opus.opus.global.security;

import com.opus.opus.global.security.handler.CustomAccessDeniedHandler;
import com.opus.opus.global.security.handler.CustomAuthenticationEntryPoint;
import com.opus.opus.global.security.oauth2.GoogleOAuth2UserService;
import com.opus.opus.global.security.oauth2.GoogleOAuth2LoginFailureHandler;
import com.opus.opus.global.security.oauth2.GoogleOAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final GoogleOAuth2UserService googleOAuth2UserService;
    private final GoogleOAuth2LoginSuccessHandler googleOAuth2LoginSuccessHandler;
    private final GoogleOAuth2LoginFailureHandler googleOAuth2LoginFailureHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(CsrfConfigurer::disable)
                .cors(Customizer.withDefaults())
                .httpBasic(HttpBasicConfigurer::disable)
                .formLogin(FormLoginConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/sign-up/**", "/sign-in/**").permitAll()
                        .requestMatchers("/oauth2/set-redirect").permitAll()
                        .requestMatchers(HttpMethod.GET, "/teams/**", "/contests/**", "/notices/**").permitAll()
                        .anyRequest().hasAnyRole("회원", "관리자", "팀장", "팀원")
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(googleOAuth2UserService))
                        .successHandler(googleOAuth2LoginSuccessHandler)
                        .failureHandler(googleOAuth2LoginFailureHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
