package mas.curs.infsys.controllers;

import mas.curs.infsys.services.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/metrics")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class MetricsController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping
    public String metricsPage(Model model) {

        Map<String, Long> authorStats = wishlistService.getAuthorWishlistStats();
        model.addAttribute("authorStats", authorStats);
        model.addAttribute("authorLabels", new ArrayList<>(authorStats.keySet()));
        model.addAttribute("authorCounts", new ArrayList<>(authorStats.values()));


        Map<String, Long> genreStats = wishlistService.getGenreWishlistStats();
        model.addAttribute("genreStats", genreStats);
        model.addAttribute("genreLabels", new ArrayList<>(genreStats.keySet()));
        model.addAttribute("genreCounts", new ArrayList<>(genreStats.values()));


        Map<String, Long> timelineStats = wishlistService.getWishlistTimelineStats();
        model.addAttribute("timelineStats", timelineStats);
        model.addAttribute("timelineLabels", new ArrayList<>(timelineStats.keySet()));
        model.addAttribute("timelineCounts", new ArrayList<>(timelineStats.values()));

        return "metrics";
    }
}

