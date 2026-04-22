package com.hrlee.transnaviserver.springboot.config.security;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.security.jwt.JwtFilter;
import com.hrlee.transnaviserver.springboot.security.jwt.JwtSecretKeyHolder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig implements LoggAble {

    private final JwtSecretKeyHolder jwtSecretKeyHolder;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin(new Customizer<FormLoginConfigurer<HttpSecurity>>() {
            @Override
            public void customize(FormLoginConfigurer<HttpSecurity> httpSecurityFormLoginConfigurer) {
                httpSecurityFormLoginConfigurer.disable();
            }
        });

        httpSecurity.httpBasic(new Customizer<HttpBasicConfigurer<HttpSecurity>>() {
            @Override
            public void customize(HttpBasicConfigurer<HttpSecurity> httpSecurityHttpBasicConfigurer) {
                httpSecurityHttpBasicConfigurer.disable();
            }
        });

        httpSecurity.csrf(new Customizer<CsrfConfigurer<HttpSecurity>>() {
            @Override
            public void customize(CsrfConfigurer<HttpSecurity> httpSecurityCsrfConfigurer) {
                httpSecurityCsrfConfigurer.disable();
            }
        });

        httpSecurity.exceptionHandling(new Customizer<ExceptionHandlingConfigurer<HttpSecurity>>() {
            @Override
            public void customize(ExceptionHandlingConfigurer<HttpSecurity> httpSecurityExceptionHandlingConfigurer) {
                httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        writeSecurityLog("ACCESS DENIED", request);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                });

                httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        writeSecurityLog("AUTH ENTRY POINT", request);
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                });
            }
        });
        httpSecurity.sessionManagement(new Customizer<SessionManagementConfigurer<HttpSecurity>>() {
            @Override
            public void customize(SessionManagementConfigurer<HttpSecurity> httpSecuritySessionManagementConfigurer) {
                httpSecuritySessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            }
        });
        httpSecurity.authorizeHttpRequests(new Customizer<AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry>() {
            @Override
            public void customize(
                    AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry
                            authorizationManagerRequestMatcherRegistry) {
                authorizationManagerRequestMatcherRegistry.requestMatchers("/token/all").permitAll();
                authorizationManagerRequestMatcherRegistry.requestMatchers("/user/registration").permitAll();
                authorizationManagerRequestMatcherRegistry.requestMatchers(HttpMethod.GET, "/place/**").permitAll();
                authorizationManagerRequestMatcherRegistry.requestMatchers("/route/**").permitAll();
                authorizationManagerRequestMatcherRegistry.anyRequest().authenticated();
            }
        });
        httpSecurity.addFilterBefore(new JwtFilter(jwtSecretKeyHolder), UsernamePasswordAuthenticationFilter.class);
        return httpSecurity.build();
    }

    private void writeSecurityLog(String category, HttpServletRequest request) {
        getLogger().error(category + " " + request.getRemoteAddr() + " " + request.getRequestURL() + " " + request.getHeader("user-agent"));
    }

}
