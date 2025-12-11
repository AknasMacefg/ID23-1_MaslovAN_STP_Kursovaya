package mas.curs.infsys.controllers;

import mas.curs.infsys.exceptions.ResourceNotFoundException;
import mas.curs.infsys.models.Role;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;


/**
 * Веб-контроллер для управления пользователями (доступен только пользователям с ролью {@code SUPER_ADMIN}).
 * <p>
 * Обеспечивает отображение списка пользователей, изменение их ролей и удаление записей.
 * Используется в административной панели через Thymeleaf-шаблон {@code users.html}.
 * </p>
 */
@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {


    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final UserService userService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String userPage(Model model, 
                          @RequestParam(required = false) String msg,
                          @RequestParam(required = false) String role,
                          @RequestParam(required = false) String sortBy,
                          @RequestParam(required = false) String search) {
        model.addAttribute("users", userService.getUsersFilteredAndSorted(role, sortBy, search));
        model.addAttribute("allRoles", Arrays.copyOfRange(Role.values(), 0, 3));
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("searchQuery", search);
        model.addAttribute("message", msg);
        return "users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
        }
        model.addAttribute("user", user);
        model.addAttribute("allRoles", Arrays.copyOfRange(Role.values(), 0, 3));
        return "edit-user";
    }

    // Handler to process the form submission (POST request)
    @PostMapping("/edit/{id}")
    public String updateItem(@PathVariable("id") Long id, @ModelAttribute("user") User userDetails, RedirectAttributes redirectAttributes) {
        User existingUser = userService.getUserById(id);
        if (existingUser == null) {
            throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
        }
        userService.addUser(userDetails);
        redirectAttributes.addFlashAttribute("success", "Пользователь успешно обновлен");
        return "redirect:/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(id);
        if (user == null) {
            throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
        }
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Пользователь успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении пользователя: " + e.getMessage());
        }
        return "redirect:/users";
    }

}
