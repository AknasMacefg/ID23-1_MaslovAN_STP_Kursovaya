package mas.curs.infsys.services;

import mas.curs.infsys.models.Book;
import mas.curs.infsys.models.Genre;
import mas.curs.infsys.repositories.GenreRepository;
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
public class GenreService {
    private static final Logger log = LoggerFactory.getLogger(GenreService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private GenreRepository genreRepository;
    
    @Autowired
    @Lazy
    private BookService bookService;
    
    @Autowired
    private mas.curs.infsys.repositories.BookGenreRepository bookGenreRepository;

    public List<Genre> getAllGenres()
    {
        return genreRepository.findAll();
    }

    public List<Genre> getGenresSorted(String sortBy, String search, int page, int pageSize) {
        List<Genre> genres = genreRepository.findAll();
        
        // Apply search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            genres = genres.stream()
                .filter(genre -> {
                    if (genre.getName() != null && genre.getName().toLowerCase().contains(searchLower)) return true;
                    if (genre.getDescription() != null && genre.getDescription().toLowerCase().contains(searchLower)) return true;
                    return false;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy) {
                case "name":
                    genres.sort(Comparator.comparing(Genre::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                    break;
                case "name_desc":
                    genres.sort(Comparator.comparing(Genre::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed());
                    break;
            }
        }
        
        // Apply pagination if more than pageSize items
        if (genres.size() > pageSize) {
            int start = page * pageSize;
            int end = Math.min(start + pageSize, genres.size());
            if (start < genres.size()) {
                return genres.subList(start, end);
            } else {
                return new java.util.ArrayList<>();
            }
        }
        
        return genres;
    }
    
    public int getTotalPages(String sortBy, String search, int pageSize) {
        List<Genre> genres = getGenresSorted(sortBy, search, 0, Integer.MAX_VALUE);
        return (int) Math.ceil((double) genres.size() / pageSize);
    }

    public Genre getGenreById(long id) {
        return genreRepository.findById(id).get();
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteGenre(Long genreId) {
        // Delete all BookGenre relationships using repository
        List<mas.curs.infsys.models.BookGenre> bookGenres = bookGenreRepository.findAll().stream()
            .filter(bg -> bg.getGenre() != null && bg.getGenre().getId().equals(genreId))
            .collect(java.util.stream.Collectors.toList());
        bookGenreRepository.deleteAll(bookGenres);
        
        genreRepository.delete(genreRepository.findById(genreId).get());
    }

    public boolean addGenre(Genre genre) {
        if (genreRepository.existsByName(genre.getName().toLowerCase(Locale.ROOT))) {
            return false;
        }
        genre.setName(genre.getName().toLowerCase(Locale.ROOT));
        genreRepository.save(genre);
        return true;
    }

    public boolean updateGenre(Genre genre) {
        if (genreRepository.existsById(genre.getId())) {
            String lowerName = genre.getName().toLowerCase(Locale.ROOT);
            // Check if name exists, but exclude the current genre being edited
            java.util.Optional<Genre> existingGenreWithName = genreRepository.findByName(lowerName);
            
            if (existingGenreWithName.isPresent() && !existingGenreWithName.get().getId().equals(genre.getId())) {
                // Name exists and belongs to a different genre
                return false;
            }
            genre.setName(lowerName);
            genreRepository.save(genre);
            return true;
        }
        return false;
    }


}
