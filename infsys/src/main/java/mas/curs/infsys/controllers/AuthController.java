package mas.curs.infsys.controllers;

import mas.curs.infsys.models.Role;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.EmailService;
import mas.curs.infsys.services.UserService;
import mas.curs.infsys.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private WishlistService wishlistService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }


    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               @RequestParam("confirmPassword") String confirmPassword,
                               RedirectAttributes redirectAttributes) {
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            redirectAttributes.addFlashAttribute("error", "Пароль должен содержать минимум 6 символов");
            return "redirect:/register";
        }
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

    @PostMapping("/profile/change-email")
    public String changeEmail(@RequestParam("password") String password,
                             @RequestParam("newEmail") String newEmail,
                             RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Validate email format (basic check)
        if (newEmail == null || newEmail.trim().isEmpty() || !newEmail.contains("@")) {
            redirectAttributes.addFlashAttribute("error", "Некорректный формат email");
            return "redirect:/profile";
        }

        // Change email
        boolean success = userService.changeEmail(currentUser.getId(), password, newEmail.trim());
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Email успешно изменен");
        } else {
            // Check if it's a password error or email already taken
            User user = userService.getUserById(currentUser.getId());
            if (user != null && !passwordEncoder.matches(password, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Неверный пароль");
            } else {
                redirectAttributes.addFlashAttribute("error", "Этот email уже занят другим пользователем");
            }
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

    @PostMapping("/profile/test-email")
    public String sendTestEmail(RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Check if user is admin
        if (currentUser.getRole() == null || currentUser.getRole() != Role.ADMIN) {
            redirectAttributes.addFlashAttribute("error", "Доступ запрещен");
            return "redirect:/profile";
        }

        try {
            emailService.sendTestEmail(currentUser.getEmail(), currentUser.getUsername());
            redirectAttributes.addFlashAttribute("success", "Тестовое письмо отправлено на " + currentUser.getEmail());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отправке тестового письма: " + e.getMessage());
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
