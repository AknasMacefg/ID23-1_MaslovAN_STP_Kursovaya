package mas.curs.infsys.controllers;
import mas.curs.infsys.exceptions.ResourceNotFoundException;
import mas.curs.infsys.models.Genre;
import mas.curs.infsys.services.GenreService;
import mas.curs.infsys.services.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/genres")
public class GenreController {
    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final GenreService genreService;
    private final BookService bookService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public GenreController(GenreService genreService, BookService bookService) {
        this.genreService = genreService;
        this.bookService = bookService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String genrePage(Model model, 
                           @RequestParam(required = false) String msg,
                           @RequestParam(required = false) String sortBy,
                           @RequestParam(required = false) String search,
                           @RequestParam(defaultValue = "0") int page) {
        int pageSize = 50;
        int totalPages = genreService.getTotalPages(sortBy, search, pageSize);
        List<Genre> allGenres = genreService.getGenresSorted(sortBy, search, 0, Integer.MAX_VALUE);
        
        model.addAttribute("genres", genreService.getGenresSorted(sortBy, search, page, pageSize));
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", allGenres.size());
        model.addAttribute("showPagination", allGenres.size() > pageSize);
        model.addAttribute("message", msg);
        return "genres";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Genre genre = genreService.getGenreById(id);
        if (genre == null) {
            throw new ResourceNotFoundException("Жанр с ID " + id + " не найден");
        }
        model.addAttribute("genre", genre);
        return "edit-genre";
    }

    // Handler to process the form submission (POST request)
    @PostMapping("/edit/{id}")
    public String updateGenre(@PathVariable("id") Long id, @ModelAttribute("genre") Genre genreDetails, Model model, RedirectAttributes redirectAttributes) {
        Genre existingGenre = genreService.getGenreById(id);
        if (existingGenre == null) {
            throw new ResourceNotFoundException("Жанр с ID " + id + " не найден");
        }
        boolean success = genreService.updateGenre(genreDetails);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Жанр успешно обновлен");
            redirectAttributes.addAttribute("id", genreDetails.getId());
            return "redirect:/genres/view/{id}";
        } else {
            redirectAttributes.addFlashAttribute("error", "Данное имя уже занято");
            redirectAttributes.addAttribute("id", genreDetails.getId());
            return "redirect:/genres/edit/{id}";
        }
    }

    @GetMapping("/view/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Genre genre = genreService.getGenreById(id);
        if (genre == null) {
            throw new ResourceNotFoundException("Жанр с ID " + id + " не найден");
        }
        model.addAttribute("genre", genre);
        model.addAttribute("books", bookService.getBooksByGenre(id));
        return "view-genre";
    }

    @GetMapping("/edit/new")
    public String showNewGenreForm(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("genre", new Genre());
        if (error != null) {
            model.addAttribute("error", "Такой жанр уже создан!");
        }
        return "edit-genre";
    }

    @PostMapping("/edit/new")
    public String addGenre(@ModelAttribute("genre") Genre genre, RedirectAttributes redirectAttributes) {
        boolean success = genreService.addGenre(genre);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Жанр успешно создан");
            return "redirect:/genres";
        } else {
            redirectAttributes.addFlashAttribute("error", "Такой жанр уже создан!");
            return "redirect:/genres/edit/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteGenre(@PathVariable("id") Long id) {
        Genre genre = genreService.getGenreById(id);
        if (genre == null) {
            throw new ResourceNotFoundException("Жанр с ID " + id + " не найден");
        }
        genreService.deleteGenre(id);
        return "redirect:/genres";
    }

}

