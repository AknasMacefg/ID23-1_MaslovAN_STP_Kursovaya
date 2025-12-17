package mas.curs.infsys.configs;

import jakarta.annotation.PostConstruct;
import mas.curs.infsys.models.User;
import mas.curs.infsys.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import mas.curs.infsys.services.UserService;

import java.time.LocalDateTime;


@Component
public class AppInitializer {
    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @PostConstruct
    public void init() {
        createUserIfNotExists("admin", "admin@example.com", true, "password", Role.ADMIN, LocalDateTime.now());
    }

    private void createUserIfNotExists(String username, String email, boolean email_notification,
                                       String rawPassword, Role role, LocalDateTime created_at) {
        if (!userService.existsUserByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setEmail_notification(email_notification);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setCreated_at(created_at);
            userService.addUser(user);
            System.out.println("Создан пользователь " + role + ": " + email);
        }
    }

}
