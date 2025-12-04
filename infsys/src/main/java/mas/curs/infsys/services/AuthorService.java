package mas.curs.infsys.services;

import mas.curs.infsys.models.Author;
import mas.curs.infsys.repositories.AuthorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AuthorService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private AuthorRepository authorRepository;

    public List<Author> getAllAuthors()
    {
        return authorRepository.findAll();
    }

    public Author getAuthorById(long id) {
        return authorRepository.findById(id).get();
    }

    public void deleteAuthor(Long authorId) {
        authorRepository.delete(authorRepository.findById(authorId).get());
    }

    public boolean addAuthor(Author author) {
        authorRepository.save(author);
        return true;
    }

    public boolean updateAuthor(Author author) {
        authorRepository.save(author);
        return true;

    }


}
