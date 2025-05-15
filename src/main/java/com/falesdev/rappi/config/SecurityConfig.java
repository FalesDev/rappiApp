package com.falesdev.rappi.config;

import com.falesdev.rappi.domain.dto.response.ApiErrorResponse;
import com.falesdev.rappi.repository.UserRepository;
import com.falesdev.rappi.security.auth.JwtAuthenticationFilter;
import com.falesdev.rappi.security.RappiUserDetails;
import com.falesdev.rappi.security.service.RappiUserDetailsService;
import com.falesdev.rappi.service.AuthenticationService;
import com.falesdev.rappi.security.service.CustomOAuth2UserService;
import com.falesdev.rappi.service.JwtService;
import com.falesdev.rappi.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Bean
    public AuthenticationSuccessHandler oauthSuccessHandler() {
        return (request, response, authentication) -> {
            RappiUserDetails userDetails = (RappiUserDetails) authentication.getPrincipal();
            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = refreshTokenService.createRefreshToken(userDetails.getId()).getToken();

            response.sendRedirect("/api/v1/auth/oauth-success?token="+accessToken+"&refreshToken="+refreshToken);

            /*// Redirigir al frontend web con el token como parámetro
            response.sendRedirect("http://localhost:5173/oauth-success?token="+accessToken+
                    "&refreshToken="+refreshToken);*/
        };
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationService authenticationService) {
        return new JwtAuthenticationFilter(authenticationService);
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new RappiUserDetailsService(userRepository);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://rappiapp-xzjeh.ondigitalocean.app",
                "https://cyber-blog-fawn.vercel.app",
                "http://localhost:5173",
                "http://localhost:4200",
                "http://10.0.2.2:8080"
        ));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth ->auth
                        // Endpoints de Swagger
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html"
                        ).permitAll()

                        // Endpoints de autenticación
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/phone/verify",
                                "/api/v1/auth/register",
                                "/api/v1/auth/email",
                                "/api/v1/auth/google",
                                "/api/v1/auth/google/phone/verify",
                                "/api/v1/auth/refresh"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/oauth-success").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/auth/me").authenticated()

                        // Endpoints administrativos
                        .requestMatchers(HttpMethod.GET,"/api/v1/roles/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")

                        // Endpoints de direcciones (usuarios autenticados)
                        .requestMatchers("/api/v1/me/addresses/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(this.customOAuth2UserService)
                        )
                        .successHandler(oauthSuccessHandler())
                )
                .csrf(csrf ->csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler())
                        .authenticationEntryPoint(authenticationEntryPoint())
                );
        return http.build();
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message("You don't have permission to access this resource")
                    .build();

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            ApiErrorResponse error = ApiErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Authentication failed: Invalid authentication token")
                    .build();

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            new ObjectMapper().writeValue(response.getWriter(), error);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }
}
