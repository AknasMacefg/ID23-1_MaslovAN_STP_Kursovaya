package mas.curs.infsys.services;

import mas.curs.infsys.models.Book;
import mas.curs.infsys.models.Series;
import mas.curs.infsys.repositories.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeriesService {
    private static final Logger log = LoggerFactory.getLogger(SeriesService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private SeriesRepository seriesRepository;
    
    @Autowired
    @Lazy
    private BookService bookService;

    public List<Series> getAllSeriess()
    {
        return seriesRepository.findAll();
    }

    public List<Series> getSeriesSorted(String sortBy, String search, int page, int pageSize) {
        List<Series> series = seriesRepository.findAll();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            series = series.stream()
                .filter(s -> {
                    if (s.getName() != null && s.getName().toLowerCase().contains(searchLower)) return true;
                    if (s.getDescription() != null && s.getDescription().toLowerCase().contains(searchLower)) return true;
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "name":
                    series.sort(Comparator.comparing(Series::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                    break;
                case "name_desc":
                    series.sort(Comparator.comparing(Series::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());
                    break;
            }
        }
        
        // Apply pagination if more than pageSize items
        if (series.size() > pageSize) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, series.size());
            if (start < series.size()) {
                return series.subList(start, end);
            } else {
                return new java.util.ArrayList<>();
            }
        }
        
        return series;
    }
    
    public int getTotalPages(String sortBy, String search, int pageSize) {
        List<Series> series = getSeriesSorted(sortBy, search, 0, Integer.MAX_VALUE);
        return (int) Math.ceil((double) series.size() / pageSize);
    }

    public Series getSeriesById(long id) {
        return seriesRepository.findById(id).get();
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteSeries(Long seriesId) {
        // Remove all BookSeries relationships before deleting the series
        List<Book> books = bookService.getBooksBySeries(seriesId);
        for (Book book : books) {
            book.getBookSeries().removeIf(bs -> bs.getSeries().getId().equals(seriesId));
            bookService.updateBook(book);
        }
        seriesRepository.delete(seriesRepository.findById(seriesId).get());
    }

    public boolean addSeries(Series series) {
        if (seriesRepository.existsByName(series.getName().toLowerCase(Locale.ROOT))) {
            return false;
        }
        series.setName(series.getName().toLowerCase(Locale.ROOT));
        seriesRepository.save(series);
        return true;
    }

    public boolean updateSeries(Series series) {
        if (seriesRepository.existsById(series.getId())) {
            if (seriesRepository.existsByName(series.getName().toLowerCase(Locale.ROOT))) {
                return false;
            }
            series.setName(series.getName().toLowerCase(Locale.ROOT));
            seriesRepository.save(series);
            return true;
        }
        return false;
    }


}
