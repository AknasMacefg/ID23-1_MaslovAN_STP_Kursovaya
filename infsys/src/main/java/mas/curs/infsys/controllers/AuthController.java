package mas.curs.infsys.controllers;

import mas.curs.infsys.models.User;
import mas.curs.infsys.services.UserService;
import mas.curs.infsys.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Веб-контроллер для обработки регистрации и входа пользователей.
 * <p>
 * Обеспечивает отображение форм регистрации и авторизации,
 * а также обработку данных, введённых пользователями через веб-интерфейс.
 * </p>
 */
@Controller
public class AuthController {

    /** Сервис для выполнения операций с пользователями. */
    @Autowired
    private UserService userService;

    @Autowired
    private WishlistService wishlistService;

    /**
     * Отображает форму регистрации нового пользователя.
     *
     * @param model объект {@link Model}, используемый для передачи данных в представление
     * @return имя шаблона страницы регистрации
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    /**
     * Обрабатывает отправку формы регистрации.
     *
     * @param user объект {@link User}, связанный с данными из формы
     * @param model объект {@link Model} для передачи ошибок при неудачной регистрации
     * @return перенаправление на страницу входа при успешной регистрации
     *         или возврат на форму с сообщением об ошибке, если пользователь уже существует
     */
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        // Validate password length
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Пароль должен содержать минимум 6 символов");
            return "redirect:/register";
        }
        
        // Validate password confirmation
        if (!user.getPassword().equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Пароли не совпадают");
            return "redirect:/register";
        }
        
        boolean success = userService.register(user);
        if (success) {
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Такой пользователь уже существует");
            return "redirect:/register";
        }
    }

    /**
     * Отображает страницу входа пользователя.
     *
     * @return имя шаблона страницы входа (login)
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Load user with wishlist
        User user = userService.getUserByIdWithWishlist(currentUser.getId());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update-notification")
    public String updateEmailNotification(@RequestParam(value = "email_notification", required = false) String emailNotificationParam,
                                         RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        
        // When checkbox is checked: browser sends "true" (checkbox value)
        // When checkbox is unchecked: browser sends "false" (hidden input)
        // Convert string to boolean
        boolean emailNotification = "true".equals(emailNotificationParam);
        
        // Always update to ensure the value is saved correctly
        userService.updateEmailNotification(currentUser.getId(), emailNotification);
        redirectAttributes.addFlashAttribute("success", "Настройки уведомлений обновлены");
        
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Validate new password and confirmation match
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Новые пароли не совпадают");
            return "redirect:/profile";
        }

        // Validate password length
        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Новый пароль должен содержать минимум 6 символов");
            return "redirect:/profile";
        }

        // Change password
        boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Пароль успешно изменен");
        } else {
            redirectAttributes.addFlashAttribute("error", "Неверный старый пароль");
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/wishlist/remove/{bookId}")
    public String removeFromWishlist(@PathVariable("bookId") Long bookId,
                                    RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        boolean success = wishlistService.removeFromWishlist(currentUser.getId(), bookId);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Книга удалена из списка желаний");
        } else {
            redirectAttributes.addFlashAttribute("error", "Произошла ошибка при удалении");
        }
        return "redirect:/profile";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getName().equals("anonymousUser")) {
            try {
                String email = authentication.getName();
                return userService.getUserByEmail(email);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
