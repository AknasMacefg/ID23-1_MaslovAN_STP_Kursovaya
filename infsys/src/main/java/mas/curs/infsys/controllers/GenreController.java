package mas.curs.infsys.controllers;
import mas.curs.infsys.models.Genre;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.GenreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/genres")
public class GenreController {
    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final GenreService genreService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String genrePage(Model model, @RequestParam(required = false) String msg) {
        model.addAttribute("genres", genreService.getAllGenres());
        model.addAttribute("message", msg);
        return "genres";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Genre genre = genreService.getGenreById(id); // Retrieve the existing item
        model.addAttribute("genre", genre);
        return "edit-genre"; // Name of your edit Thymeleaf template
    }

    // Handler to process the form submission (POST request)
    @PostMapping("/edit/{id}")
    public String updateItem(@ModelAttribute("user") Genre genreDetails) {
        // Here you would use your service to save the updated item details
        genreService.addGenre(genreDetails);
        return "redirect:/genres/{id}"; // Redirect back to the list page after saving
    }

    @GetMapping("/edit/new")
    public String showNewGenreForm(Model model) {
        model.addAttribute("genre", new Genre());
        return "edit-genre";
    }

    @PostMapping("/edit/new")
    public String registerUser(@ModelAttribute("genre") Genre genre, Model model) {
        boolean success = genreService.addGenre(genre);
        if (success) {
            return "redirect:/genres";
        } else {
            model.addAttribute("error", "Такой жанр уже создан!");
            return "redirect:/genres/edit/new?error";
        }
    }

}

