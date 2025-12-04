package mas.curs.infsys.controllers;
import mas.curs.infsys.models.Series;
import mas.curs.infsys.models.User;
import mas.curs.infsys.services.SeriesService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/series")
public class SeriesController {
    /** Репозиторий пользователей, обеспечивающий доступ к данным. */
    private final SeriesService seriesService;

    /**
     * Конструктор контроллера пользователей.
     *
     * @param userRepository репозиторий пользователей
     */
    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }

    /**
     * Отображает панель управления пользователями.
     *
     * @param model объект {@link Model} для передачи данных в шаблон (список пользователей и сообщения)
     * @param msg необязательное сообщение (используется для отображения статуса операции)
     * @return имя Thymeleaf-шаблона страницы пользователей ({@code users})
     */
    @GetMapping
    public String seriesPage(Model model, @RequestParam(required = false) String msg) {
        model.addAttribute("series", seriesService.getAllSeriess());
        model.addAttribute("message", msg);
        return "series";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Series series = seriesService.getSeriesById(id); // Retrieve the existing item
        model.addAttribute("series", series);
        return "edit-series"; // Name of your edit Thymeleaf template
    }

    // Handler to process the form submission (POST request)
    @PostMapping("/edit/{id}")
    public String updateSeries(@ModelAttribute("series") Series seriesDetails, Model model, RedirectAttributes redirectAttributes) {
        boolean success = seriesService.updateSeries(seriesDetails);
        if (success) {
            redirectAttributes.addAttribute("id", seriesDetails.getId());
            return "redirect:/series/{id}"; // Works correctly now
        } else {
            model.addAttribute("error", "Данное имя уже занято.");
            return "redirect:/seriess/edit/{id}?error";
        }
    }

    @GetMapping("/{id}")
    public String showViewForm(@PathVariable("id") Long id, Model model) {
        Series series = seriesService.getSeriesById(id); // Retrieve the existing item
        model.addAttribute("series", series);
        return "view-series"; // Name of your edit Thymeleaf template
    }

    @GetMapping("/edit/new")
    public String showNewSeriesForm(Model model) {
        model.addAttribute("series", new Series());
        return "edit-series";
    }

    @PostMapping("/edit/new")
    public String addSeries(@ModelAttribute("series") Series series, Model model) {
        boolean success = seriesService.addSeries(series);
        if (success) {
            return "redirect:/series";
        } else {
            model.addAttribute("error", "Такая серия уже создана!");
            return "redirect:/series/edit/new?error";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSeries(@PathVariable("id") Long id) {
        seriesService.deleteSeries(id);
        return "redirect:/series";
    }

}

