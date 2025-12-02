package mas.curs.infsys.services;

import mas.curs.infsys.models.Genre;
import mas.curs.infsys.repositories.GenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class GenreService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private GenreRepository genreRepository;

    public List<Genre> getAllGenres()
    {
        return genreRepository.findAll();
    }

    public Genre getGenreById(long id) {
        return genreRepository.findById(id).get();
    }

    public void deleteGenre(Long genreId) {
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

}
