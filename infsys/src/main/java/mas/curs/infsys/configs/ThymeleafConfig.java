package mas.curs.infsys.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

/**
 * Конфигурационный класс Thymeleaf.
 * <p>
 * Отвечает за интеграцию шаблонизатора Thymeleaf со Spring Security.
 * Добавляет поддержку security-тегов через диалект {@link SpringSecurityDialect}.
 * </p>
 */
@Configuration
public class ThymeleafConfig {

    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
