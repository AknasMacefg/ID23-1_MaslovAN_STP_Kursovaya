package mas.curs.infsys.controllers;
import mas.curs.infsys.exceptions.ResourceNotFoundException;
import mas.curs.infsys.models.Series;
import mas.curs.infsys.services.SeriesService;
import mas.curs.infsys.services.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/series")
public class SeriesController {
    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final SeriesService seriesService;
    private final BookService bookService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public SeriesController(SeriesService seriesService, BookService bookService) {
        this.seriesService = seriesService;
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
    public String seriesPage(Model model, 
                             @RequestParam(required = false) String msg,
                             @RequestParam(required = false) String sortBy,
                             @RequestParam(required = false) String search,
                             @RequestParam(defaultValue = "0") int page) {
        int pageSize = 50;
        int totalPages = seriesService.getTotalPages(sortBy, search, pageSize);
        List<Series> allSeries = seriesService.getSeriesSorted(sortBy, search, 0, Integer.MAX_VALUE);
        
        model.addAttribute("series", seriesService.getSeriesSorted(sortBy, search, page, pageSize));
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("searchQuery", search);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", allSeries.size());
        model.addAttribute("showPagination", allSeries.size() > pageSize);
        model.addAttribute("message", msg);
        return "series";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Series series = seriesService.getSeriesById(id);
        if (series == null) {
            throw new ResourceNotFoundException("Серия с ID " + id + " не найдена");
        }
        model.addAttribute("series", series);
        return "edit-series";
    }

    // Handler to process the form submission (POST request)
    @PostMapping("/edit/{id}")
    public String updateSeries(@PathVariable("id") Long id, @ModelAttribute("series") Series seriesDetails, Model model, RedirectAttributes redirectAttributes) {
        Series existingSeries = seriesService.getSeriesById(id);
        if (existingSeries == null) {
            throw new ResourceNotFoundException("Серия с ID " + id + " не найдена");
        }
        boolean success = seriesService.updateSeries(seriesDetails);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Серия успешно обновлена");
            redirectAttributes.addAttribute("id", seriesDetails.getId());
            return "redirect:/series/view/{id}";
        } else {
            redirectAttributes.addFlashAttribute("error", "Данное имя уже занято");
            redirectAttributes.addAttribute("id", seriesDetails.getId());
            return "redirect:/series/edit/{id}";
        }
    }

    @GetMapping("/view/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Series series = seriesService.getSeriesById(id);
        if (series == null) {
            throw new ResourceNotFoundException("Серия с ID " + id + " не найдена");
        }
        model.addAttribute("series", series);
        model.addAttribute("books", bookService.getBooksBySeries(id));
        return "view-series";
    }

    @GetMapping("/edit/new")
    public String showNewSeriesForm(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("series", new Series());
        if (error != null) {
            model.addAttribute("error", "Такая серия уже создана!");
        }
        return "edit-series";
    }

    @PostMapping("/edit/new")
    public String addSeries(@ModelAttribute("series") Series series, RedirectAttributes redirectAttributes) {
        boolean success = seriesService.addSeries(series);
        if (success) {
            redirectAttributes.addFlashAttribute("success", "Серия успешно создана");
            return "redirect:/series";
        } else {
            redirectAttributes.addFlashAttribute("error", "Такая серия уже создана!");
            return "redirect:/series/edit/new";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSeries(@PathVariable("id") Long id) {
        Series series = seriesService.getSeriesById(id);
        if (series == null) {
            throw new ResourceNotFoundException("Серия с ID " + id + " не найдена");
        }
        seriesService.deleteSeries(id);
        return "redirect:/series";
    }

}

