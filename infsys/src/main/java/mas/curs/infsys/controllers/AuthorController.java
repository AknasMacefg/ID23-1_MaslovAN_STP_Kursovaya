package mas.curs.infsys.controllers;
import mas.curs.infsys.models.Author;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.AuthorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/author")
public class AuthorController {
    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final AuthorService authorService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String authorPage(Model model, @RequestParam(required = false) String msg) {
        model.addAttribute("author", authorService.getAllAuthors());
        model.addAttribute("message", msg);
        return "author";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Author author = authorService.getAuthorById(id); // Retrieve the existing item
        model.addAttribute("author", author);
        return "edit-author"; // Name of your edit Thymeleaf template
    }

    // Handler to process the form submission (POST request)
    @PostMapping("/edit/{id}")
    public String updateAuthor(@ModelAttribute("author") Author authorDetails, Model model, RedirectAttributes redirectAttributes) {
        boolean success = authorService.updateAuthor(authorDetails);
        if (success) {
            redirectAttributes.addAttribute("id", authorDetails.getId());
            return "redirect:/authors/{id}"; // Works correctly now
        } else {
            model.addAttribute("error", "Данное имя уже занято.");
            return "redirect:/authors/edit/{id}?error";
        }
    }

    @GetMapping("/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Author author = authorService.getAuthorById(id); // Retrieve the existing item
        model.addAttribute("author", author);
        return "view-author"; // Name of your edit Thymeleaf template
    }

    @GetMapping("/edit/new")
    public String showNewAuthorForm(Model model) {
        model.addAttribute("author", new Author());
        return "edit-author";
    }

    @PostMapping("/edit/new")
    public String addAuthor(@ModelAttribute("author") Author author, Model model) {
        boolean success = authorService.addAuthor(author);
        if (success) {
            return "redirect:/authors";
        } else {
            model.addAttribute("error", "Такой автор уже создан!");
            return "redirect:/authors/edit/new?error";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id) {
        authorService.deleteAuthor(id);
        return "redirect:/authors";
    }

}

