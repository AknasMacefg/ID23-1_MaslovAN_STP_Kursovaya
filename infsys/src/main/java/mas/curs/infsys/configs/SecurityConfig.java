package mas.curs.infsys.configs;

import mas.curs.infsys.services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * Конфигурационный класс безопасности Spring Security.
 * <p>
 * Определяет:
 * <ul>
 *     <li>правила доступа к ресурсам приложения,</li>
 *     <li>настройку страниц входа и выхода,</li>
 *     <li>обработку ошибок доступа и аутентификации.</li>
 * </ul>
 * </p>
 */
@Configuration
public class SecurityConfig {

    /** Кастомная реализация {@link org.springframework.security.core.userdetails.UserDetailsService} для загрузки пользователей. */
    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")   // применяется только к API
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Все запросы к /api/** явно запрещены.
                        .anyRequest().denyAll()
                );



        return http.build();
    }



    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http,
                                          LoginSuccessHandler loginSuccessHandler,
                                          CustomLogoutSuccessHandler logoutSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/about", "/contacts", "/genres", "/authors", "/series", "/books", "/books/view/**", "/authors/view/**", "/genres/view/**", "/series/view/**", "/login", "/register", "/styles/**", "/images/**", "/scripts/**", "/h2-console/**", "/error")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .successHandler(loginSuccessHandler)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutSuccessHandler)
                        .deleteCookies("remember-me")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeServices(rememberMeServices())
                        .key("infsys-remember-me-secret-key-2024")
                        .tokenValiditySeconds(86400 * 30) // 30 days
                )
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }


    /**
     * Регистрирует кодировщик паролей, использующий алгоритм BCrypt.
     *
     * @return экземпляр {@link PasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Настраивает сервис "Запомнить меня" для сохранения сессии пользователя.
     * <p>
     * Использует токен-основанный подход, который сохраняет информацию о пользователе
     * в cookie браузера. При следующем посещении пользователь будет автоматически аутентифицирован.
     * </p>
     *
     * @return настроенный сервис {@link TokenBasedRememberMeServices}
     */
    @Bean
    public TokenBasedRememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices rememberMeServices = new TokenBasedRememberMeServices(
                "infsys-remember-me-secret-key-2024",
                userDetailsService
        );
        rememberMeServices.setCookieName("remember-me");
        rememberMeServices.setTokenValiditySeconds(86400 * 30); // 30 days
        rememberMeServices.setAlwaysRemember(false); // Remember only if checkbox is checked
        return rememberMeServices;
    }
}
