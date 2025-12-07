package mas.curs.infsys.controllers;

import mas.curs.infsys.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class CalendarController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public String mainPage(Model model) {
        LocalDate today = LocalDate.now();
        
        // Get all upcoming release dates (only for unreleased books with status SOON)
        List<LocalDate> releaseDates = bookService.getUpcomingReleaseDates();
        
        // Get books for each date (only unreleased books with status SOON)
        java.util.Map<LocalDate, List<mas.curs.infsys.models.Book>> booksByDate = new java.util.LinkedHashMap<>();
        for (LocalDate date : releaseDates) {
            booksByDate.put(date, bookService.getBooksByReleaseDate(date));
        }
        
        model.addAttribute("booksByDate", booksByDate);
        model.addAttribute("releaseDates", releaseDates);
        model.addAttribute("today", today);
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        
        return "main";
    }
}
