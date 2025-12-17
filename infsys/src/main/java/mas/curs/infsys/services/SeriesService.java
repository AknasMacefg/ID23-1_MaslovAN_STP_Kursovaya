package mas.curs.infsys.services;


import mas.curs.infsys.models.Series;
import mas.curs.infsys.repositories.SeriesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class SeriesService {

    @Autowired
    private SeriesRepository seriesRepository;
    
    @Autowired
    @Lazy
    private BookService bookService;
    
    @Autowired
    private mas.curs.infsys.repositories.BookSeriesRepository bookSeriesRepository;

    public List<Series> getAllSeriess()
    {
        return seriesRepository.findAll();
    }

    public List<Series> getSeriesSorted(String sortBy, String search, int page, int pageSize) {
        List<Series> series = seriesRepository.findAll();
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

        List<mas.curs.infsys.models.BookSeries> bookSeries = bookSeriesRepository.findAll().stream()
            .filter(bs -> bs.getSeries() != null && bs.getSeries().getId().equals(seriesId))
            .collect(java.util.stream.Collectors.toList());
        bookSeriesRepository.deleteAll(bookSeries);
        
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
            String lowerName = series.getName().toLowerCase(Locale.ROOT);

            java.util.Optional<Series> existingSeriesWithName = seriesRepository.findByName(lowerName);
            
            if (existingSeriesWithName.isPresent() && !existingSeriesWithName.get().getId().equals(series.getId())) {

                return false;
            }
            series.setName(lowerName);
            seriesRepository.save(series);
            return true;
        }
        return false;
    }


}
