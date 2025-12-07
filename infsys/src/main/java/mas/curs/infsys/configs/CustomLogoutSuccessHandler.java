package mas.curs.infsys.configs;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mas.curs.infsys.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    private UserService userService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                 Authentication authentication) throws IOException, ServletException {
        if (authentication != null) {
            String email = authentication.getName();
            userService.updateLogoutTime(email);
        }
        response.sendRedirect("/login?logout");
    }
}


