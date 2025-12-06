package mas.curs.infsys.controllers;

import mas.curs.infsys.services.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class CalendarController {

    @Autowired
    private BookService bookService;

    @GetMapping("/")
    public String mainPage(Model model, 
                           @RequestParam(required = false) String selectedDate) {
        LocalDate today = LocalDate.now();
        LocalDate targetDate = today;
        
        // Parse selected date if provided
        if (selectedDate != null && !selectedDate.isEmpty()) {
            try {
                targetDate = LocalDate.parse(selectedDate);
            } catch (Exception e) {
                targetDate = today;
            }
        }
        
        // Get all upcoming release dates
        List<LocalDate> releaseDates = bookService.getUpcomingReleaseDates();
        
        // If no dates found or target date not in list, use today
        if (releaseDates.isEmpty() || !releaseDates.contains(targetDate)) {
            if (!releaseDates.isEmpty()) {
                targetDate = releaseDates.get(0);
            }
        }
        
        // Get books for each date
        java.util.Map<LocalDate, List<mas.curs.infsys.models.Book>> booksByDate = new java.util.LinkedHashMap<>();
        for (LocalDate date : releaseDates) {
            booksByDate.put(date, bookService.getBooksByReleaseDate(date));
        }
        
        model.addAttribute("booksByDate", booksByDate);
        model.addAttribute("releaseDates", releaseDates);
        model.addAttribute("selectedDate", targetDate.toString());
        model.addAttribute("today", today);
        model.addAttribute("dateFormatter", DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        
        return "main";
    }
}
