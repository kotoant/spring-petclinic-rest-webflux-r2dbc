//package org.springframework.samples.petclinic.security;
//
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//
///**
// * Starting from Spring Boot 2, if Spring Security is present, endpoints are secured by default
// * using Spring Security’s content-negotiation strategy.
// */
//@Configuration
//@ConditionalOnProperty(name = "petclinic.security.enable", havingValue = "false")
//public class DisableSecurityConfig {
//
//    @Bean
//    public SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
//        // @formatter:off
//        http
//            .authorizeExchange((authz) -> authz
//                .anyExchange().permitAll()
//            .and()
//            .csrf()
//                .disable());
//        // @formatter:on
//        return http.build();
//    }
//}
